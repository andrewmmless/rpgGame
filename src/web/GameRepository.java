import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import java.util.*;

/** Each accepted command and its save commit together. Version checks reject stale browser tabs. */
@org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
public class GameRepository {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ObjectMapper json=JsonMapper.builder().build();
    public GameRepository(JdbcTemplate jdbc,PlatformTransactionManager manager) {this.jdbc=jdbc;transactions=new TransactionTemplate(manager);
        // Preserve accounts made in the first local preview before the private schema was introduced.
        try {
            for(Map<String,Object> row:jdbc.queryForList("SELECT username,password_hash FROM public.rpg_users")) {
                String user=(String)row.get("username");
                if(jdbc.queryForObject("SELECT COUNT(*) FROM hearthglen.rpg_users WHERE username=?",Integer.class,user)==0)
                    jdbc.update("INSERT INTO hearthglen.rpg_users(username,password_hash) VALUES (?,?)",user,row.get("password_hash"));
            }
            for(Map<String,Object> old:jdbc.queryForList("SELECT username,version,payload FROM public.rpg_saves")) {
                String user=(String)old.get("username");
                if(row(user)==null)jdbc.update("INSERT INTO hearthglen.rpg_saves(username,version,payload) VALUES (?,?,?)",user,old.get("version"),old.get("payload"));
            }
        } catch(org.springframework.jdbc.BadSqlGrammarException absentLegacyTables) { /* Normal for a fresh installation. */ }
        for(Map<String,Object> old:jdbc.queryForList("SELECT username,payload FROM hearthglen.rpg_saves"))
            scores((String)old.get("username"),decode((String)old.get("payload")),null);
    }
    public void register(String username,String password,PasswordEncoder encoder) {
        if(username==null || !username.matches("[a-zA-Z0-9_]{3,24}"))throw new IllegalArgumentException("Use 3–24 letters, numbers, or underscores for your account name.");
        if(password==null || password.length()<10 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Use a password of at least 10 characters (at most 72 bytes).");
        try {jdbc.update("INSERT INTO hearthglen.rpg_users(username,password_hash) VALUES (?,?)",username.toLowerCase(Locale.ROOT),encoder.encode(password));}
        catch(DuplicateKeyException e){throw new IllegalArgumentException("That account name is already taken.");}
    }
    private record Row(long version,String payload) {}
    private Row row(String user) {
        return jdbc.query("SELECT version,payload FROM hearthglen.rpg_saves WHERE username=?",(rs,n)->new Row(rs.getLong(1),rs.getString(2)),user).stream().findFirst().orElse(null);
    }
    private GameSession decode(String text) {
        try {return GameSession.restore(json.readValue(text,GameSave.class),new Random());}
        catch(Exception e){throw new IllegalStateException("Could not read your save. Your stored progress has not been changed.",e);}
    }
    private String encode(GameSession game) {
        try {return json.writeValueAsString(game.snapshot());}
        catch(Exception e){throw new IllegalStateException("Could not prepare save.",e);}
    }
    private Map<String,Object> response(String user,GameSession game,long version) {
        Map<String,Object> result=new LinkedHashMap<>(game.view());result.put("coopActive",jdbc.queryForObject("SELECT COUNT(*) FROM hearthglen.rpg_coop_members m JOIN hearthglen.rpg_coop p ON p.id=m.party_id WHERE m.username=? AND p.active=TRUE",Integer.class,user)>0);result.put("version",version);result.put("generation",CharacterSlots.generation(jdbc,user));return result;
    }
    public Map<String,Object> load(String user) {
        return transactions.execute(status->{SocialStore.lock(jdbc);Row row=row(user);return row==null?Map.of("needsCharacter",true,"generation",CharacterSlots.generation(jdbc,user)):response(user,decode(row.payload()),row.version());});
    }
    public Map<String,Object> create(String user,String name,String className,long generation) {
        if(name==null || !name.matches("[\\p{L}\\p{N} _-]{1,40}") || name.isBlank())throw new IllegalArgumentException("Use 1–40 letters, numbers, spaces, underscores or dashes for your character name.");
        PlayerClass type;
        try {type=PlayerClass.valueOf(className);}catch(Exception e){throw new IllegalArgumentException("Choose a class.");}
        GameSession game=new GameSession(type.create(name.strip()),new Random());return transactions.execute(status->{SocialStore.lock(jdbc);CharacterSlots.validate(jdbc,user,generation);for(String text:jdbc.query("SELECT payload FROM hearthglen.rpg_character_slots WHERE username=? AND slot<>?",(r,n)->r.getString(1),user,CharacterSlots.active(jdbc,user)))if(decode(text).snapshot().player().type()==type)throw new IllegalArgumentException("You already have a character of this class in another slot.");return insert(user,game,true);});
    }
    private Map<String,Object> insert(String user,GameSession game,boolean ranked) {
        return transactions.execute(status -> {
            try {jdbc.update("INSERT INTO hearthglen.rpg_saves(username,version,payload) VALUES (?,0,?)",user,encode(game));}
            catch(DuplicateKeyException e){throw new IllegalArgumentException("You already have a character. Reload to continue.");}
            scores(user,game,ranked);
            return response(user,game,0);
        });
    }
    public Map<String,Object> command(String user,long expectedVersion,String action,String value,long generation) {
        return transactions.execute(status -> {
            SocialStore.lock(jdbc);CharacterSlots.validate(jdbc,user,generation);
            jdbc.queryForList("SELECT username FROM hearthglen.rpg_saves WHERE username=? FOR UPDATE",user);
            int active=jdbc.queryForObject("SELECT COUNT(*) FROM hearthglen.rpg_coop_members m JOIN hearthglen.rpg_coop p ON p.id=m.party_id WHERE m.username=? AND p.active=TRUE",Integer.class,user);
            if(active>0)throw new IllegalArgumentException("Leave or finish your co-op party before playing solo.");
            Row row=row(user);
            if(row==null)throw new IllegalArgumentException("Create a character first.");
            if(row.version()!=expectedVersion)throw new StaleGameException();
            GameSession game=decode(row.payload());game.command(action,value);
            int updated=jdbc.update("UPDATE hearthglen.rpg_saves SET payload=?,version=version+1 WHERE username=? AND version=?",encode(game),user,expectedVersion);
            if(updated!=1)throw new StaleGameException();
            scores(user,game,null);
            return response(user,game,expectedVersion+1);
        });
    }
    void scores(String user,GameSession game,Boolean ranked) {
        GameSave save=game.snapshot();
        int count=jdbc.update("UPDATE hearthglen.rpg_scores SET character_name=?,player_class=?,level=?,kills=?,bosses=?,tower=? WHERE username=?",
            save.player().name(),save.player().type().name(),save.player().level(),save.kills(),save.cleared().size(),save.towerBest(),user);
        if(count==0)jdbc.update("INSERT INTO hearthglen.rpg_scores(username,character_name,player_class,level,kills,bosses,tower,ranked) VALUES (?,?,?,?,?,?,?,?)",
            user,save.player().name(),save.player().type().name(),save.player().level(),save.kills(),save.cleared().size(),save.towerBest(),Boolean.TRUE.equals(ranked));
    }
    public List<Map<String,Object>> leaderboard() {
        return jdbc.query("SELECT character_name,player_class,level,kills,bosses,tower,ranked FROM hearthglen.rpg_scores WHERE ranked = TRUE ORDER BY tower DESC,bosses DESC,level DESC,kills DESC,username ASC",
            (rs,n)->Map.<String,Object>of("name",rs.getString(1),"type",rs.getString(2),"level",rs.getInt(3),"kills",rs.getInt(4),"bosses",rs.getInt(5),"tower",rs.getInt(6),"ranked",rs.getBoolean(7)));
    }
    public String export(String user) {
        Row row=row(user);if(row==null)throw new IllegalArgumentException("No character to export.");return row.payload();
    }
    public static final class StaleGameException extends RuntimeException { private static final long serialVersionUID=1L; }
}
