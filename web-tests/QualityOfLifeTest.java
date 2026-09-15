import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class QualityOfLifeTest {
    @Test void bulkSaleIsAtomicAndProtectsEquippedItems(){
        GameSave s=new GameSession(new Mage("Test"),new Random(1)).snapshot();
        Equipment equipped=new Equipment("equipped","Staff",Equipment.Slot.WEAPON,Equipment.Rarity.RARE,1,5,0);
        Equipment a=new Equipment("a","Coat",Equipment.Slot.ARMOUR,Equipment.Rarity.COMMON,1,2,0),b=new Equipment("b","Mail",Equipment.Slot.ARMOUR,Equipment.Rarity.RARE,2,5,0);
        GameSession game=GameSession.restore(new GameSave(1,s.player(),s.mode(),s.area(),0,0,0,0,0,s.cleared(),s.claimed(),List.of(equipped,a,b),Map.of(Equipment.Slot.WEAPON,"equipped"),s.log(),null),new Random(1));
        GameSave before=game.snapshot();
        for(String value:List.of("a,equipped","a,missing","a,a","")){assertThrows(IllegalArgumentException.class,()->game.command("sell_many",value));assertEquals(before,game.snapshot());}
        game.command("protect_item","a");
        GameSession restored=GameSession.restore(game.snapshot(),new Random(1));
        assertEquals(game.snapshot(),restored.snapshot());
        GameSession protectedGame=game;GameSave protectedSave=game.snapshot();
        assertThrows(IllegalArgumentException.class,()->protectedGame.command("sell","a"));
        assertThrows(IllegalArgumentException.class,()->protectedGame.command("sell_many","a,b"));assertEquals(protectedSave,game.snapshot());
        game.command("protect_item","a");
        game.command("sell_many","a,b");assertEquals(List.of(equipped),game.snapshot().inventory());assertEquals(a.value()+b.value(),game.snapshot().player().coins());
        GameSave sold=game.snapshot();assertThrows(IllegalArgumentException.class,()->game.command("sell_many","a,b"));assertEquals(sold,game.snapshot());
    }
    @Test void resaleCannotProfitFromShoppingOrUpgradingAndNamesMatchClass(){
        assertEquals("Ashwood Staff",Equipment.weaponName(PlayerClass.MAGE,Equipment.Rarity.COMMON));
        assertEquals("Iron Mace",Equipment.weaponName(PlayerClass.CLERIC,Equipment.Rarity.COMMON));
        assertEquals("Iron Daggers",Equipment.weaponName(PlayerClass.ROGUE,Equipment.Rarity.COMMON));
        for(int level:List.of(1,25,100))for(Equipment.Rarity rarity:Equipment.Rarity.values()){
            Equipment item=new Equipment("test","Item",Equipment.Slot.WEAPON,rarity,level,2,0);
            assertTrue(item.value()<20+level*6);assertTrue(item.upgrade().value()-item.value()<item.upgradeCost());
        }
    }
}
