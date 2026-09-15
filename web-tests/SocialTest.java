import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class SocialTest {
    @Test void rallyRequiresBondAndCannotStackWhenBothPlayersChooseIt(){
        CoopDungeon p=new CoopDungeon();p.add("a",new GameSession(new Warrior("A"),new Random(1)).snapshot());p.add("b",new GameSession(new Mage("B"),new Random(1)).snapshot());p.start("a",0);
        assertThrows(IllegalArgumentException.class,()->p.choose("a",0,"RALLY",1,new Random(1)));
        p.duoUnlocked=true;p.members.get(0).health=30;p.members.get(1).health=30;p.choose("a",0,"RALLY",1,new Random(1));p.choose("b",0,"RALLY",1,new Random(1));
        assertTrue(p.duoUsed);assertEquals(38,p.members.get(1).health);assertThrows(IllegalArgumentException.class,()->p.choose("a",1,"RALLY",2,new Random(1)));
    }
    @Test void earlyRegionDropsStayEarlyEvenForHighLevelPlayers(){
        Random lowest=new Random(){@Override public int nextInt(int bound){return 0;}@Override public boolean nextBoolean(){return false;}};
        Equipment early=Equipment.regionalDrop(lowest,100,true,PlayerClass.MAGE,Area.WHISPERING_WOODS);
        assertEquals(15,early.level());assertEquals(Equipment.Rarity.RARE,early.rarity());assertTrue(early.name().startsWith("Woodland"));
        Equipment late=Equipment.regionalDrop(lowest,15,true,PlayerClass.MAGE,Area.FORGOTTEN_RUINS);assertEquals(Equipment.Rarity.EPIC,late.rarity());assertTrue(late.name().startsWith("Relic"));
    }
}
