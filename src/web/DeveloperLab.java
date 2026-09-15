import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.security.Principal;
import java.util.*;

/** Isolated test characters. Never reads or writes normal saves or leaderboard scores. */
@RestController
@RequestMapping("/api/developer")
@org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
public class DeveloperLab {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Environment environment;
    private final JsonMapper json=JsonMapper.builder().build();
    public DeveloperLab(JdbcTemplate jdbc,PlatformTransactionManager manager,Environment environment) {
        this.jdbc=jdbc;this.transactions=new TransactionTemplate(manager);this.environment=environment;
    }
    private boolean allowed(Principal principal) {
        String configured=environment.getProperty("DEVELOPER_USERNAME","").strip().toLowerCase(Locale.ROOT);
        return !configured.isEmpty() && principal!=null && configured.equals(principal.getName());
    }
    private String authorize(Principal principal) {
        if(!allowed(principal))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Developer access is not enabled for this account.");
        return principal.getName();
    }
    @GetMapping("/me") public Map<String,Boolean> access(Principal principal) {return Map.of("enabled",allowed(principal));}
    private record Row(long version,String payload) {}
    private Row row(String user) {return jdbc.query("SELECT version,payload FROM hearthglen.rpg_developer_saves WHERE username=?",(rs,n)->new Row(rs.getLong(1),rs.getString(2)),user).stream().findFirst().orElse(null);}
    private GameSession decode(String payload) {return GameSession.restore(json.readValue(payload,GameSave.class),new Random());}
    private Map<String,Object> response(GameSession game,long version) {Map<String,Object> result=new LinkedHashMap<>(game.view());result.put("version",version);result.put("developer",true);return result;}
    @GetMapping("/game") public Map<String,Object> load(Principal principal) {
        Row row=row(authorize(principal));return row==null?Map.of("needsCharacter",true,"developer",true):response(decode(row.payload()),row.version());
    }
    @PostMapping("/character") public Map<String,Object> create(Principal principal,@RequestBody GameController.Creation body) {
        String user=authorize(principal);GameSession game=preset(body.playerClass());
        return transactions.execute(status->{
            try{jdbc.update("INSERT INTO hearthglen.rpg_developer_saves(username,version,payload) VALUES (?,0,?)",user,json.writeValueAsString(game.snapshot()));}
            catch(org.springframework.dao.DuplicateKeyException e){throw new IllegalArgumentException("A test character already exists. Return to the lab.");}
            return response(game,0);
        });
    }
    @PostMapping("/command") public Map<String,Object> command(Principal principal,@RequestBody GameController.Command command) {
        String user=authorize(principal);
        return transactions.execute(status->{
            Row row=row(user);if(row==null)throw new IllegalArgumentException("Create a test character first.");
            if(row.version()!=command.version())throw new GameRepository.StaleGameException();
            GameSession game=decode(row.payload());
            if("dev_reset".equals(command.action()))game=preset(command.value());
            else if("dev_level".equals(command.action())) {
                int level;try{level=Integer.parseInt(command.value());}catch(Exception e){throw new IllegalArgumentException("Choose a level from 1 to 100.");}
                if(level<1||level>100)throw new IllegalArgumentException("Choose a level from 1 to 100.");
                GameSave s=game.snapshot();Player p=s.player().type().create(s.player().name());p.setLevel(level);p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());p.setCoins(s.player().coins());p.setPotions(99);
                GameSave.PlayerData pd=new GameSave.PlayerData(p.getName(),p.getPlayerClass(),level,0,p.getHealth(),p.getResource(),p.getCoins(),99,0);
                game=GameSession.restore(new GameSave(1,pd,"TOWN",s.area(),0,s.kills(),s.deaths(),s.towerBest(),0,s.cleared(),s.claimed(),s.inventory(),s.equipped(),s.log(),null),new Random());
            } else game.command(command.action(),command.value());
            if(jdbc.update("UPDATE hearthglen.rpg_developer_saves SET payload=?,version=version+1 WHERE username=? AND version=?",json.writeValueAsString(game.snapshot()),user,command.version())!=1)throw new GameRepository.StaleGameException();
            return response(game,command.version()+1);
        });
    }
    private GameSession preset(String className) {
        PlayerClass type;try{type=PlayerClass.valueOf(className);}catch(Exception e){throw new IllegalArgumentException("Choose a class.");}
        Player p=type.create("Developer "+className.toLowerCase(Locale.ROOT));p.setLevel(25);p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());p.setCoins(100000);p.setPotions(99);
        GameSave s=new GameSession(p,new Random()).snapshot();List<Equipment> gear=new ArrayList<>();
        for(Equipment.Slot slot:Equipment.Slot.values())for(Equipment.Rarity rarity:Equipment.Rarity.values())gear.add(new Equipment(UUID.randomUUID().toString(),"Test "+rarity.name().toLowerCase(Locale.ROOT)+" "+slot.name().toLowerCase(Locale.ROOT),slot,rarity,25,14+rarity.ordinal()*3,0,slot==Equipment.Slot.WEAPON?WeaponAttribute.values()[1+rarity.ordinal()]:WeaponAttribute.NONE));
        return GameSession.restore(new GameSave(1,s.player(),"TOWN",Area.WHISPERING_WOODS,0,3,0,0,0,new HashSet<>(Arrays.stream(Area.values()).map(Enum::name).toList()),Set.of(),gear,Map.of(),List.of("Developer sandbox: unranked. Normal character and scores are separate."),null),new Random());
    }
}
