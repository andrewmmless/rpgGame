import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class GatheringTest {
    @Test void skillsMaterialsAndCostsPersistWithoutOpeningStoryRoutes(){
        GameSession game=new GameSession(new Mage("Gatherer"),new Random(1));
        assertThrows(IllegalArgumentException.class,()->game.command("gather","MINING:0"));
        assertThrows(IllegalArgumentException.class,()->game.command("gather","FORAGING:1"));
        game.command("gather","FORAGING:0");assertEquals(32,game.snapshot().player().resource());
        GameSession restored=GameSession.restore(game.snapshot(),new Random(1));assertEquals(game.snapshot(),restored.snapshot());
        assertTrue(restored.snapshot().claimed().contains("gather:FORAGING:xp:25"));assertTrue(restored.snapshot().cleared().isEmpty());
        for(int i=0;i<4;i++)restored.command("gather","FORAGING:0");
        GameSave before=restored.snapshot();assertThrows(IllegalArgumentException.class,()->restored.command("gather","FORAGING:0"));assertEquals(before,restored.snapshot());
        restored.command("sell_material","FORAGING:0");assertEquals(10,restored.snapshot().player().coins());
    }
    @Test void gatheringRequiresStoryAndSkillEvenWithHighCombatLevel(){
        Set<String> flags=new HashSet<>(Set.of("gather:FORAGING:xp:600"));Player p=new Mage("Test");p.setLevel(60);
        assertThrows(IllegalArgumentException.class,()->Gathering.FORAGING.gather(2,p,flags,Set.of(),new Random(1)));
        flags.add(SubArea.get(0).key());flags.add(SubArea.get(1).key());
        assertDoesNotThrow(()->Gathering.FORAGING.gather(2,p,flags,Set.of(),new Random(1)));
    }
}
