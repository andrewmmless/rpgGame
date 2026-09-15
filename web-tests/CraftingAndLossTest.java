import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CraftingAndLossTest {
    @Test void craftingChecksEverythingBeforeSpendingAndCannotRepeatForFree(){
        Player p=new Mage("Crafter");p.setLevel(5);p.setCoins(30);
        Set<String> flags=new HashSet<>(Set.of("gather:FORAGING:item0:8"));
        assertThrows(IllegalArgumentException.class,()->Crafting.craft("trail_coat",p,flags,Set.of(),30));assertEquals(30,p.getCoins());assertEquals(8,Gathering.FORAGING.owned(flags,0));
        Equipment item=Crafting.craft("trail_coat",p,flags,Set.of(),0);assertEquals(Equipment.Slot.ARMOUR,item.slot());assertEquals(18,p.getCoins());assertEquals(0,Gathering.FORAGING.owned(flags,0));
        assertThrows(IllegalArgumentException.class,()->Crafting.craft("trail_coat",p,flags,Set.of(),0));assertEquals(18,p.getCoins());
    }
    @Test void fallenCoopPlayerLosesProgressButNeverLevelsOrEquipment(){
        Player p=new Mage("Fallen");p.setLevel(5);p.setXp(100);p.setCoins(100);
        GameSave s=new GameSession(p,new Random(1)).snapshot();
        GameSession game=GameSession.restore(new GameSave(1,s.player(),s.mode(),s.area(),0,0,0,0,0,Set.of(),Set.of("gather:FORAGING:item0:20"),s.inventory(),s.equipped(),s.log(),null),new Random(1));
        game.returnFromCoop(0,0,0,0,0,null,false);
        assertEquals(5,game.snapshot().player().level());assertEquals(85,game.snapshot().player().xp());assertEquals(90,game.snapshot().player().coins());assertEquals(19,Gathering.FORAGING.owned(game.snapshot().claimed(),0));
        GameSave after=game.snapshot();game.returnFromCoop(1,0,0,0,0,null,false);assertEquals(after.player().xp(),game.snapshot().player().xp());
    }
}
