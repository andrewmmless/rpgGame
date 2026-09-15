import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CoopTest {
    @Test void missingPartnerCanDefendAfterDeadlineAndInvalidMoveDoesNotConsumeTurn(){
        CoopDungeon p=new CoopDungeon();p.add("a",new GameSession(new Warrior("A"),new Random(1)).snapshot());p.add("b",new GameSession(new Mage("B"),new Random(1)).snapshot());p.start("a",0);
        assertThrows(IllegalArgumentException.class,()->p.choose("a",0,"FAKE",1,new Random(1)));
        assertNull(p.members.get(0).action);p.choose("a",0,"PROTECT",1,new Random(1));
        assertThrows(IllegalArgumentException.class,()->p.cover(0,59999,new Random(1)));
        p.cover(0,60000,new Random(1));assertEquals(1,p.round);assertTrue(p.members.stream().allMatch(m->m.action==null));
        assertThrows(IllegalArgumentException.class,()->p.choose("a",0,"ATTACK",60001,new Random(1)));
    }
}
