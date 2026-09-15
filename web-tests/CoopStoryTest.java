import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import tools.jackson.databind.json.JsonMapper;
class CoopStoryTest {
    private GameSave save(String name,int level,Set<String> flags){
        Player p=new Warrior(name);p.setLevel(level);p.setHealth(p.getMaxHealth());
        GameSave s=new GameSession(p,new Random(1)).snapshot();
        return new GameSave(1,s.player(),s.mode(),s.area(),0,0,0,0,0,s.cleared(),flags,s.inventory(),s.equipped(),s.log(),null);
    }
    private CoopDungeon party(int route){CoopDungeon p=new CoopDungeon();p.selectStory(route);Set<String> flags=new HashSet<>();for(int i=0;i<route;i++)flags.add(SubArea.get(i).key());p.add("a",save("A",60,flags));p.add("b",save("B",60,flags));p.start("a",0);return p;}
    private void round(CoopDungeon p,String a,String b){int r=p.round;p.choose("a",r,a,1,new Random(1));p.choose("b",r,b,1,new Random(1));}
    @Test void bothPlayersMustHaveRouteUnlocked(){CoopDungeon p=new CoopDungeon();p.selectStory(1);p.add("a",save("A",10,Set.of(SubArea.get(0).key())));assertThrows(IllegalArgumentException.class,()->p.add("b",save("B",10,Set.of())));}
    @Test void objectiveAndPendingMoveSurviveRestartAndThreeStagesAreRequired(){
        CoopDungeon p=party(0);p.choose("a",0,"OBJECTIVE",1,new Random(1));
        JsonMapper json=JsonMapper.builder().build();p=json.readValue(json.writeValueAsString(p),CoopDungeon.class);assertEquals("OBJECTIVE",p.members.get(0).action);
        p.choose("b",0,"OBJECTIVE",1,new Random(1));assertEquals(2,p.objectiveProgress);
        for(int stage=0;stage<3;stage++){
            assertEquals(stage,p.stage);round(p,"OBJECTIVE","OBJECTIVE");p.enemyHealth=1;round(p,"ATTACK","OBJECTIVE");
            assertEquals(stage==2?"VICTORY":"BATTLE",p.state);
        }
    }
    @Test void neglectingConvoyFailsAndNoStoryCanBeClaimedEarly(){
        CoopDungeon p=party(0);p.integrity=10;round(p,"DEFEND","DEFEND");assertEquals("DEFEAT",p.state);
        GameSession g=GameSession.restore(save("A",10,Set.of()),new Random(1));assertThrows(IllegalArgumentException.class,()->g.completeCoopStory(2));
        int bonus=g.completeCoopStory(0);assertEquals(100,bonus);assertEquals(0,g.completeCoopStory(0));assertTrue(g.snapshot().claimed().contains(SubArea.get(0).key()));
    }
    @Test void searchesAreRequiredAfterEnemyDies(){
        CoopDungeon p=party(1);p.enemyHealth=1;round(p,"ATTACK","ATTACK");assertEquals(0,p.stage);assertEquals("BATTLE",p.state);
        round(p,"OBJECTIVE","OBJECTIVE");round(p,"OBJECTIVE","DEFEND");assertEquals(1,p.stage);
    }
    @Test void wardWorkReducesEnemyDamage(){
        CoopDungeon normal=party(2),disrupted=party(2);
        round(normal,"DEFEND","ATTACK");round(disrupted,"DEFEND","OBJECTIVE");
        assertTrue(disrupted.members.get(0).health>normal.members.get(0).health);
    }
}
