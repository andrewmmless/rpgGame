import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class StoryPathTest {
    @Test void commissionPersistsAndReportIsIdempotent() {
        GameSession game=new GameSession(new Mage("Test"),new Random(1));
        assertThrows(IllegalArgumentException.class,()->game.command("story","accept"));
        game.command("story","brief");game.command("story","accept");
        GameSave s=game.snapshot();
        assertThrows(IllegalArgumentException.class,()->game.command("story","report"));
        GameSession veteran=GameSession.restore(new GameSave(s.schemaVersion(),s.player(),s.mode(),s.area(),s.room(),s.kills(),s.deaths(),s.towerBest(),s.towerFloor(),Set.of("WHISPERING_WOODS"),s.claimed(),s.inventory(),s.equipped(),s.log(),s.battle()),new Random(1));
        veteran.command("story","report");GameSave reported=veteran.snapshot();veteran.command("story","report");
        assertEquals(reported.claimed(),veteran.snapshot().claimed());assertEquals(reported.player(),veteran.snapshot().player());
        GameSession restored=GameSession.restore(veteran.snapshot(),new Random(1));
        assertEquals(true,((Map<?,?>)restored.view().get("story")).get("reported"));
    }
}
