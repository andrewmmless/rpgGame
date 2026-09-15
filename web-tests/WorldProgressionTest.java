import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class WorldProgressionTest {
    @Test void storyClearsUnlockRoutesWhileLevelsCannotBypassThem() {
        Player high=new Mage("High");high.setLevel(100);
        GameSession locked=new GameSession(high,new Random(1));
        assertTrue(locked.unlocked(Area.WHISPERING_WOODS));assertFalse(locked.unlocked(Area.STONEFANG_CAVES));
        assertThrows(IllegalArgumentException.class,()->locked.command("adventure","DRAGONS_SPIRE"));
        assertThrows(IllegalArgumentException.class,()->locked.command("adventure","WHISPERING_WOODS:2"));
        assertEquals(5,locked.view().get("shopLevel"));
        GameSave s=new GameSession(new Mage("Low"),new Random(1)).snapshot();
        GameSession opened=GameSession.restore(new GameSave(s.schemaVersion(),s.player(),s.mode(),s.area(),s.room(),s.kills(),s.deaths(),s.towerBest(),s.towerFloor(),Set.of("WHISPERING_WOODS"),s.claimed(),s.inventory(),s.equipped(),s.log(),s.battle()),new Random(1));
        assertTrue(opened.unlocked(Area.STONEFANG_CAVES));assertFalse(opened.unlocked(Area.FORGOTTEN_RUINS));
        opened.command("adventure","STONEFANG_CAVES");assertEquals(Area.STONEFANG_CAVES,opened.snapshot().area());
    }
}
