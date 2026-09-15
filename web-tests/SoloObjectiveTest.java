import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class SoloObjectiveTest {
    @Test void objectiveConsumesATurnAndPersists(){
        Player p=new Warrior("Guard");p.setLevel(5);p.setHealth(p.getMaxHealth());
        GameSession g=new GameSession(p,new Random(1));g.command("adventure","WHISPERING_WOODS:0");g.command("continue","");
        int enemyHp=g.snapshot().battle().health(),hp=g.snapshot().player().health();g.command("objective","");
        assertEquals(enemyHp,g.snapshot().battle().health());assertTrue(g.snapshot().player().health()<hp);
        assertTrue(g.snapshot().claimed().contains("mission:work:1"));
        GameSession restored=GameSession.restore(g.snapshot(),new Random(1));assertEquals(g.snapshot(),restored.snapshot());restored.command("objective","");assertTrue(restored.snapshot().claimed().contains("mission:work:2"));
    }
    @Test void convoyFailureCannotGrantClear(){
        Player p=new Warrior("Guard");p.setLevel(5);p.setHealth(p.getMaxHealth());GameSession g=new GameSession(p,new Random(1));g.command("adventure","WHISPERING_WOODS:0");g.command("continue","");GameSave s=g.snapshot();Set<String> flags=new HashSet<>(s.claimed());flags.remove("mission:integrity:100");flags.add("mission:integrity:5");
        g=GameSession.restore(new GameSave(1,s.player(),s.mode(),s.area(),s.room(),0,0,0,0,s.cleared(),flags,s.inventory(),s.equipped(),s.log(),s.battle()),new Random(1));g.command("combat","DEFEND");
        assertEquals("DEFEAT",g.snapshot().mode());assertFalse(g.snapshot().claimed().contains("route:clear:0"));assertEquals(0,g.snapshot().deaths());
    }
}
