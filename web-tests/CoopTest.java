import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CoopTest {
    private CoopDungeon downed(){
        CoopDungeon p=new CoopDungeon();p.add("a",new GameSession(new Warrior("A"),new Random(1)).snapshot());p.add("b",new GameSession(new Mage("B"),new Random(1)).snapshot());p.start("a",0);
        p.members.get(0).health=1;p.choose("a",0,"ATTACK",1,new Random(1));p.choose("b",0,"ATTACK",1,new Random(1));assertEquals("RESCUE",p.state);return p;
    }
    @Test void rescueSurvivesSaveAndOnlyLivingPartnerCanUseItOnce(){
        var mapper=tools.jackson.databind.json.JsonMapper.builder().build();CoopDungeon p=downed();
        CoopDungeon restored=mapper.readValue(mapper.writeValueAsString(p),CoopDungeon.class);
        assertThrows(IllegalArgumentException.class,()->restored.rescue("a",10));restored.rescue("b",10);
        assertEquals("BATTLE",restored.state);assertEquals(35,restored.members.get(0).health);assertEquals(1,restored.rescuesUsed);
        assertThrows(IllegalArgumentException.class,()->restored.rescue("b",11));
        restored.members.get(0).health=1;restored.choose("a",1,"ATTACK",12,new Random(1));restored.choose("b",1,"ATTACK",12,new Random(1));assertEquals("DEFEAT",restored.state);
    }
    @Test void missedRescueExpiresWithoutRewards(){CoopDungeon p=downed();assertThrows(IllegalArgumentException.class,()->p.endRescue(2));p.endRescue(p.deadline+1);assertEquals("DEFEAT",p.state);assertTrue(p.rewards.isEmpty());}
    @Test void missingPartnerCanDefendAfterDeadlineAndInvalidMoveDoesNotConsumeTurn(){
        CoopDungeon p=new CoopDungeon();p.add("a",new GameSession(new Warrior("A"),new Random(1)).snapshot());p.add("b",new GameSession(new Mage("B"),new Random(1)).snapshot());p.start("a",0);
        assertThrows(IllegalArgumentException.class,()->p.choose("a",0,"FAKE",1,new Random(1)));
        assertNull(p.members.get(0).action);p.choose("a",0,"PROTECT",1,new Random(1));
        assertThrows(IllegalArgumentException.class,()->p.cover(0,59999,new Random(1)));
        p.cover(0,60000,new Random(1));assertEquals(1,p.round);assertTrue(p.members.stream().allMatch(m->m.action==null));
        assertThrows(IllegalArgumentException.class,()->p.choose("a",0,"ATTACK",60001,new Random(1)));
    }
}
