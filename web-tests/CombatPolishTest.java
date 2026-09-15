import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CombatPolishTest {
    @Test void guardBlocksResourceSiphon(){
        Enemy enemy=new Enemy("Frost Elemental",10,200,10,5,0,0,0,0);
        Player guarded=new Mage("Guard"),plain=new Mage("Plain");
        guarded.applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));
        enemy.performTurn(0,guarded,new Random(1),new ArrayList<>());enemy.performTurn(0,plain,new Random(1),new ArrayList<>());
        assertEquals(40,guarded.getResource());assertEquals(32,plain.getResource());assertTrue(enemy.intent(0).contains("defend"));
    }
    @Test void stunPreventsCultistRecovery(){
        Enemy enemy=new Enemy("Dark Cultist",10,200,10,5,0,0,0,0);Player player=new Warrior("Test");player.setLevel(2);
        CombatEngine engine=new CombatEngine(player,enemy,new Random(1));enemy.restoreHealth(100);
        engine.performAction(CombatAction.ABILITY,"shield_bash");assertTrue(enemy.getHealth()<100);
        Enemy recovering=new Enemy("Dark Cultist",10,200,10,5,0,0,0,0);recovering.restoreHealth(100);recovering.performTurn(0,player,new Random(1),new ArrayList<>());assertEquals(125,recovering.getHealth());
    }
    @Test void onlyLeaderCanTransferToCurrentMember(){
        GuildHouse g=new GuildHouse();g.owner="a";List<String> members=List.of("a","b");
        assertThrows(IllegalArgumentException.class,()->g.transfer("b","a",members));assertThrows(IllegalArgumentException.class,()->g.transfer("a","outsider",members));assertEquals("a",g.owner);
        g.transfer("a","b",members);assertEquals("b",g.owner);assertEquals(1,g.history.size());
    }
}
