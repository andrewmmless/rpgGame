import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class DifficultyTest {
    @Test void levelingPreservesMissingHealthAndResourceAndNeverRevives() {
        Player p=new Mage("Test");p.setHealth(15);p.setResource(3);
        p.gainXp(Balance.xpNeeded(1)+Balance.xpNeeded(2));
        assertEquals(3,p.getLevel());assertEquals(35,p.getHealth());assertEquals(7,p.getResource());
        p.setHealth(0);p.gainXp(p.getXpToNextLevel());assertTrue(p.isDead());
    }
    @Test void oldXpRemainsValidAndNewCurveNeedsMoreEncounters() {
        for(int level=1;level<Balance.MAX_LEVEL;level++) {
            Player p=new Warrior("Test");p.setLevel(level);p.setXp(30+12*(level-1)-1);
            assertTrue(p.getXpToNextLevel()>p.getXp());
            assertTrue(Balance.xpNeeded(level)>=5*Balance.enemyXp(level));
        }
    }
    @Test void defendingReducesTelegraphedHeavyAndRecoversResource() {
        Player attacker=new Warrior("Test"),defender=new Warrior("Test");attacker.setResource(0);defender.setResource(0);
        Enemy a=new Enemy("Boss",3,1000,20,0,0,0,0,0).asBoss(),b=new Enemy("Boss",3,1000,20,0,0,0,0,0).asBoss();
        CombatEngine attack=new CombatEngine(attacker,a,new Random(1)),guard=new CombatEngine(defender,b,new Random(1));
        attack.restoreProgress(1,Map.of());guard.restoreProgress(1,Map.of());
        assertTrue(a.intent(1).contains("defend"));
        attack.performAction(CombatAction.ATTACK);guard.performAction(CombatAction.DEFEND);
        assertTrue(defender.getHealth()>attacker.getHealth());assertEquals(10,defender.getResource());
    }
}
