import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.security.Principal;
import java.util.*;

/** Existing rpg_saves remains the active character; inactive snapshots are stored separately. */
@RestController
@RequestMapping("/api/characters")
public class CharacterSlots {
    private final JdbcTemplate jdbc;private final GameRepository games;private final TransactionTemplate tx;
    private final JsonMapper json=JsonMapper.builder().build();
    public CharacterSlots(JdbcTemplate jdbc,GameRepository games,PlatformTransactionManager manager){this.jdbc=jdbc;this.games=games;tx=new TransactionTemplate(manager);}
    static long generation(JdbcTemplate jdbc,String user){return jdbc.query("SELECT generation FROM hearthglen.rpg_slot_state WHERE username=?",(r,n)->r.getLong(1),user).stream().findFirst().orElse(0L);}
    static int active(JdbcTemplate jdbc,String user){return jdbc.query("SELECT active_slot FROM hearthglen.rpg_slot_state WHERE username=?",(r,n)->r.getInt(1),user).stream().findFirst().orElse(0);}
    static void validate(JdbcTemplate jdbc,String user,long expected){if(generation(jdbc,user)!=expected)throw new GameRepository.StaleGameException();}
    public record Selection(int slot,long generation) {}
    @GetMapping public Map<String,Object> list(Principal principal){return tx.execute(status->{SocialStore.lock(jdbc);return view(principal.getName());});}
    private Map<String,Object> view(String user){int active=active(jdbc,user);Map<Integer,String> saves=new HashMap<>();jdbc.query("SELECT slot,payload FROM hearthglen.rpg_character_slots WHERE username=?",r->{saves.put(r.getInt(1),r.getString(2));},user);saves.remove(active);jdbc.query("SELECT payload FROM hearthglen.rpg_saves WHERE username=?",r->{saves.put(active,r.getString(1));},user);
        List<Map<String,Object>> slots=new ArrayList<>();for(int i=0;i<4;i++){Map<String,Object> s=new LinkedHashMap<>();s.put("slot",i);s.put("active",i==active);String payload=saves.get(i);s.put("empty",payload==null);if(payload!=null){GameSave save=json.readValue(payload,GameSave.class);s.put("name",save.player().name());s.put("type",save.player().type());s.put("level",save.player().level());}slots.add(s);}return Map.of("generation",generation(jdbc,user),"slots",slots);
    }
    @PostMapping("/select") public Map<String,Object> select(Principal principal,@RequestBody Selection body){String user=principal.getName();return tx.execute(status->{SocialStore.lock(jdbc);validate(jdbc,user,body.generation());CoopDungeon.require(body.slot()>=0&&body.slot()<4,"Choose one of four slots.");int old=active(jdbc,user);if(old==body.slot())return games.load(user);
        CoopDungeon.require(jdbc.queryForObject("SELECT COUNT(*) FROM hearthglen.rpg_coop_members WHERE username=?",Integer.class,user)==0,"Leave your co-op party before switching characters.");
        String current=jdbc.query("SELECT payload FROM hearthglen.rpg_saves WHERE username=?",(r,n)->r.getString(1),user).stream().findFirst().orElse(null);
        if(current!=null){boolean ranked=Boolean.TRUE.equals(jdbc.queryForObject("SELECT ranked FROM hearthglen.rpg_scores WHERE username=?",Boolean.class,user));if(jdbc.update("UPDATE hearthglen.rpg_character_slots SET payload=?,ranked=? WHERE username=? AND slot=?",current,ranked,user,old)==0)jdbc.update("INSERT INTO hearthglen.rpg_character_slots(username,slot,payload,ranked) VALUES (?,?,?,?)",user,old,current,ranked);}
        var target=jdbc.query("SELECT payload,ranked FROM hearthglen.rpg_character_slots WHERE username=? AND slot=?",(r,n)->Map.entry(r.getString(1),r.getBoolean(2)),user,body.slot()).stream().findFirst();
        jdbc.update("DELETE FROM hearthglen.rpg_saves WHERE username=?",user);jdbc.update("DELETE FROM hearthglen.rpg_scores WHERE username=?",user);
        if(target.isPresent()){String payload=target.get().getKey();jdbc.update("INSERT INTO hearthglen.rpg_saves(username,version,payload) VALUES (?,0,?)",user,payload);games.scores(user,GameSession.restore(json.readValue(payload,GameSave.class),new Random()),target.get().getValue());}
        if(jdbc.update("UPDATE hearthglen.rpg_slot_state SET active_slot=?,generation=generation+1 WHERE username=?",body.slot(),user)==0)jdbc.update("INSERT INTO hearthglen.rpg_slot_state(username,active_slot,generation) VALUES (?,?,1)",user,body.slot());return games.load(user);
    });}
}
