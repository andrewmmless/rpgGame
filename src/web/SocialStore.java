import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;

/** All social mutations run under the short database mutex, then row/save writes in one transaction. */
@org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
public class SocialStore {
    final JdbcTemplate jdbc;final JsonMapper json=JsonMapper.builder().build();
    public SocialStore(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public static void lock(JdbcTemplate jdbc){jdbc.queryForList("SELECT id FROM hearthglen.rpg_social_lock WHERE id=1 FOR UPDATE");}
    String guildId(String user){return jdbc.query("SELECT guild_id FROM hearthglen.rpg_guild_member WHERE username=?",(r,n)->r.getString(1),user).stream().findFirst().orElse(null);}
    GuildHouse guild(String id){return jdbc.query("SELECT payload FROM hearthglen.rpg_guild WHERE id=?",(r,n)->json.readValue(r.getString(1),GuildHouse.class),id).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Guild not found."));}
    List<String> members(String id){return jdbc.query("SELECT username FROM hearthglen.rpg_guild_member WHERE guild_id=? ORDER BY username",(r,n)->r.getString(1),id);}
    void saveGuild(String id,GuildHouse g){g.version++;jdbc.update("UPDATE hearthglen.rpg_guild SET payload=? WHERE id=?",json.writeValueAsString(g),id);}
    GameSave character(String user){return jdbc.query("SELECT payload FROM hearthglen.rpg_saves WHERE username=? FOR UPDATE",(r,n)->json.readValue(r.getString(1),GameSave.class),user).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Create a normal character first."));}
    void available(String user){GameSave s=character(user);CoopDungeon.require(s.mode().equals("TOWN"),"Return to town first.");CoopDungeon.require(Boolean.TRUE.equals(jdbc.queryForObject("SELECT ranked FROM hearthglen.rpg_scores WHERE username=?",Boolean.class,user)),"Use a normal server-created character.");int n=jdbc.queryForObject("SELECT COUNT(*) FROM hearthglen.rpg_coop_members m JOIN hearthglen.rpg_coop p ON p.id=m.party_id WHERE m.username=? AND p.active=TRUE",Integer.class,user);CoopDungeon.require(n==0,"Finish or leave your co-op party first.");}
    public static class Bond {public int clears;public long lastCredit;public List<String> memories=new ArrayList<>();}
    String bondId(String a,String b){return a.compareTo(b)<0?a+":"+b:b+":"+a;}
    Bond bond(String a,String b){return jdbc.query("SELECT payload FROM hearthglen.rpg_bond WHERE id=?",(r,n)->json.readValue(r.getString(1),Bond.class),bondId(a,b)).stream().findFirst().orElse(new Bond());}
    void saveBond(String a,String b,Bond bond){String id=bondId(a,b),payload=json.writeValueAsString(bond);if(jdbc.update("UPDATE hearthglen.rpg_bond SET payload=? WHERE id=?",payload,id)==0)jdbc.update("INSERT INTO hearthglen.rpg_bond(id,payload) VALUES (?,?)",id,payload);}
    public boolean infirmary(CoopDungeon p){String id=guildId(p.members.get(0).username);return id!=null&&id.equals(guildId(p.members.get(1).username))&&guild(id).owned.contains("project:infirmary");}
    public boolean duoUnlocked(CoopDungeon p){return p.members.size()==2&&bond(p.members.get(0).username,p.members.get(1).username).clears>=3;}
    public void completed(CoopDungeon p){String a=p.members.get(0).username,b=p.members.get(1).username;Bond bond=bond(a,b);long now=System.currentTimeMillis();boolean credit=p.round>=3&&now-bond.lastCredit>=300000;
        if(credit){bond.clears++;bond.lastCredit=now;}String memory=java.time.LocalDate.now(java.time.ZoneOffset.UTC)+" · "+p.missionName()+" cleared in "+p.round+" rounds; "+p.rescuesUsed+" rescue used.";bond.memories.add(memory);while(bond.memories.size()>20)bond.memories.remove(0);saveBond(a,b,bond);
        String id=guildId(a);if(id!=null&&id.equals(guildId(b))){GuildHouse g=guild(id);if(credit)g.materials=Math.min(999,g.materials+(g.owned.contains("project:workshop")?2:1));if(p.storyRoute==null)g.owned.add("warden_trophy");if(bond.clears>=5)g.owned.add("bond_lantern");g.record(memory+(credit?" House supplies delivered.":" Bond credit is limited to one clear per five minutes."));saveGuild(id,g);}
        p.say(credit?"Bond progress +1. Complete three credited adventures together to unlock Rally.":"Victory recorded. Bond credit requires at least three rounds and five minutes between credited clears.");
    }
}
