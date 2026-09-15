import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/coop")
@org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
public class CoopController {
    private final JdbcTemplate jdbc;private final TransactionTemplate tx;private final GameRepository games;
    private final JsonMapper json=JsonMapper.builder().build();
    public CoopController(JdbcTemplate jdbc,PlatformTransactionManager manager,GameRepository games){this.jdbc=jdbc;this.tx=new TransactionTemplate(manager);this.games=games;}
    private static final java.security.SecureRandom CODES=new java.security.SecureRandom();
    private static final String ALPHABET="abcdefghjkmnpqrstuvwxyz23456789";
    private final Map<String,long[]> joinAttempts=new HashMap<>();
    private String inviteCode(){StringBuilder code=new StringBuilder();for(int i=0;i<6;i++)code.append(ALPHABET.charAt(CODES.nextInt(ALPHABET.length())));return code.toString();}
    private synchronized void joinLimit(String user){long now=System.currentTimeMillis();joinAttempts.entrySet().removeIf(e->now-e.getValue()[0]>=60000);long[] window=joinAttempts.get(user);if(window==null){if(joinAttempts.size()>=10000)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,"Try again in a minute.");window=new long[]{now,0};joinAttempts.put(user,window);}if(++window[1]>10)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,"Too many party-code attempts. Try again in a minute.");}
    public record Join(String code) {} public record Move(String id,int round,String action,String value) {}
    private String membership(String user){return jdbc.query("SELECT party_id FROM hearthglen.rpg_coop_members WHERE username=?",(rs,n)->rs.getString(1),user).stream().findFirst().orElse(null);}
    private CoopDungeon party(String id,boolean lock){return jdbc.query("SELECT payload FROM hearthglen.rpg_coop WHERE id=?"+(lock?" FOR UPDATE":""),(rs,n)->json.readValue(rs.getString(1),CoopDungeon.class),id).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Party not found."));}
    private void save(String id,CoopDungeon party){jdbc.update("UPDATE hearthglen.rpg_coop SET payload=?,active=? WHERE id=?",json.writeValueAsString(party),Set.of("LOBBY","BATTLE").contains(party.state),id);}
    private GameSave character(String user){String text=jdbc.query("SELECT payload FROM hearthglen.rpg_saves WHERE username=? FOR UPDATE",(rs,n)->rs.getString(1),user).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Create a normal character first."));return json.readValue(text,GameSave.class);}
    private GameSave eligible(String user){GameSave save=character(user);CoopDungeon.require(membership(user)==null,"Leave your existing party first.");Boolean ranked=jdbc.queryForObject("SELECT ranked FROM hearthglen.rpg_scores WHERE username=?",Boolean.class,user);CoopDungeon.require(Boolean.TRUE.equals(ranked),"Co-op requires a normal, server-created character.");return save;}
    @GetMapping public Map<String,Object> get(Principal principal){String id=membership(principal.getName());return id==null?Map.of("joined",false):view(id,party(id,false),principal.getName());}
    @PostMapping("/create") public Map<String,Object> create(Principal principal){
        for(int attempt=0;attempt<5;attempt++){try{return createParty(principal.getName());}catch(org.springframework.dao.DuplicateKeyException collision){/* Retry with a fresh transaction and code. */}}
        throw new IllegalArgumentException("Could not allocate a party code. Please try again.");
    }
    private Map<String,Object> createParty(String user){return tx.execute(status->{GameSave s=eligible(user);CoopDungeon p=new CoopDungeon();p.add(user,s);String id=inviteCode();jdbc.update("INSERT INTO hearthglen.rpg_coop(id,active,payload) VALUES (?,TRUE,?)",id,json.writeValueAsString(p));jdbc.update("INSERT INTO hearthglen.rpg_coop_members(username,party_id) VALUES (?,?)",user,id);return view(id,p,user);});}
    @PostMapping("/join") public Map<String,Object> join(Principal principal,@RequestBody Join body){String user=principal.getName();String id=body.code()==null?"":body.code().strip().toLowerCase(Locale.ROOT);joinLimit(user);CoopDungeon.require(id.matches("[a-z2-9]{6}|[a-f0-9]{16}"),"Enter the six-character party code (older codes also work).");return tx.execute(status->{CoopDungeon p=party(id,true);p.add(user,eligible(user));jdbc.update("INSERT INTO hearthglen.rpg_coop_members(username,party_id) VALUES (?,?)",user,id);save(id,p);return view(id,p,user);});}
    @PostMapping("/move") public Map<String,Object> move(Principal principal,@RequestBody Move body){String user=principal.getName();return tx.execute(status->{String id=membership(user);CoopDungeon.require(id!=null&&id.equals(body.id()),"This is not your current party.");CoopDungeon p=party(id,true);CoopDungeon.require(p.members.stream().anyMatch(m->m.username.equals(user)),"Not a party member.");String before=p.state;
        switch(body.action()==null?"":body.action()){
            case "start" -> p.start(user,System.currentTimeMillis());
            case "choose" -> p.choose(user,body.round(),body.value(),System.currentTimeMillis(),new Random());
            case "cover" -> p.cover(body.round(),System.currentTimeMillis(),new Random());
            case "leave" -> {if(Set.of("LOBBY","BATTLE").contains(p.state)){p.state="ABANDONED";p.say("A player left. The party returned to town.");}jdbc.update("DELETE FROM hearthglen.rpg_coop_members WHERE username=? AND party_id=?",user,id);}
            default -> throw new IllegalArgumentException("Unknown party action.");
        }
        if(before.equals("BATTLE")&&!p.state.equals("BATTLE"))finish(p);
        save(id,p);return body.action().equals("leave")?Map.<String,Object>of("joined",false):view(id,p,user);
    });}
    private void finish(CoopDungeon party){
        // Party row lock serializes final resolution; reward writes and completion commit together.
        for(CoopDungeon.Member member:party.members.stream().sorted(Comparator.comparing(m->m.username)).toList()){
            GameSave stored=character(member.username);GameSession game=GameSession.restore(stored,new Random());boolean won=party.state.equals("VICTORY");
            Equipment reward=won?Equipment.drop(new Random(),member.original.player().level(),true,member.original.player().type()):null;
            int used=Math.min(3,member.original.player().potions())-member.potions;
            if(won)party.rewards.add(new CoopDungeon.Reward(member.original.player().name(),party.xpReward(),party.coinReward(),reward,stored.inventory().size()>=30?reward.value():0));
            game.returnFromCoop(member.health,member.resource,used,party.xpReward(),party.coinReward(),reward,won);
            jdbc.update("UPDATE hearthglen.rpg_saves SET payload=?,version=version+1 WHERE username=?",json.writeValueAsString(game.snapshot()),member.username);games.scores(member.username,game,null);
            if(won)party.say(member.original.player().name()+" found "+reward.name()+" ("+reward.rarity().name().toLowerCase(Locale.ROOT)+").");
        }
    }
    private Map<String,Object> view(String id,CoopDungeon p,String user){Map<String,Object> out=new LinkedHashMap<>();out.put("joined",true);out.put("id",id);out.put("state",p.state);out.put("host",user.equals(p.leader));out.put("round",p.round);out.put("deadline",p.deadline);out.put("serverTime",System.currentTimeMillis());out.put("log",p.log);out.put("rewards",p.rewards);out.put("enemy",Map.of("name","Rootbound Warden","health",p.enemyHealth,"maxHealth",p.enemyMaxHealth,"intent",p.intent()));
        out.put("members",p.members.stream().map(m->{Player player=m.player();Map<String,Object> row=new LinkedHashMap<>();row.put("name",player.getName());row.put("type",player.getPlayerClass());row.put("level",player.getLevel());row.put("health",m.health);row.put("maxHealth",player.getMaxHealth());row.put("resource",m.resource);row.put("maxResource",player.getMaxResource());row.put("potions",m.potions);row.put("you",m.username.equals(user));row.put("ready",m.action!=null);row.put("action",m.action==null?"":m.action);row.put("abilities",player.getAvailableAbilities().stream().map(a->Map.of("id",a.id(),"name",a.name(),"cost",a.cost(),"cooldown",m.cooldowns.getOrDefault(a.id(),0))).toList());return row;}).toList());return out;}
}
