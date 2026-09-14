import java.util.*;
import java.nio.file.*;

public class FoundationTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Enemy enemy(int hp,int attack){return new Enemy("Test",1,hp,attack,0,2,2,10,10);}
    public static void main(String[] args)throws Exception {
        Player p=new Warrior("Test");
        int health=p.getHealth();p.takeDamage(0);check(p.getHealth()==health,"Zero damage");
        check(Balance.damage(20,50,DamageType.PHYSICAL)==10,"Smooth armour");
        check(Balance.damage(20,50,DamageType.TRUE)==20,"True damage");
        p.setResistance(DamageType.FIRE,0.5);check(p.receiveDamage(20,DamageType.FIRE)==8,"Resistance");
        p.applyStatus(new StatusEffect("burn",StatusEffect.Kind.DAMAGE_OVER_TIME,3,2));
        health=p.getHealth();p.endTurn();p.endTurn();p.endTurn();check(p.getHealth()==health-6,"Exact duration");
        check(p.getStatuses().isEmpty(),"Status expires");
        p=new Warrior("Test");Enemy e=enemy(500,8);CombatEngine engine=new CombatEngine(p,e,new Random(1));
        health=p.getHealth();int resource=p.getResource();
        check(!engine.performAction(CombatAction.ABILITY,"missing").accepted(),"Invalid ability rejected");
        check(!engine.performAction(CombatAction.POTION).accepted(),"Full health potion rejected");
        check(p.getHealth()==health&&p.getResource()==resource,"Invalid commands preserve turn");
        engine.performAction(CombatAction.ABILITY,"ko_slash");
        check(engine.getCooldown("ko_slash")==2,"Cooldown starts");
        health=p.getHealth();check(!engine.performAction(CombatAction.ABILITY,"ko_slash").accepted(),"Cooldown rejects reuse");
        check(p.getHealth()==health,"Rejected ability has no retaliation");
        engine.performAction(CombatAction.ATTACK);engine.performAction(CombatAction.ATTACK);
        check(engine.getCooldown("ko_slash")==0,"Cooldown completes after two other actions");
        p.setResource(0);check(!engine.performAction(CombatAction.ABILITY,"ko_slash").accepted(),"Cost enforced");
        p=new Warrior("Test");e=enemy(1,100);engine=new CombatEngine(p,e,new Random(1));
        engine.performAction(CombatAction.ATTACK);check(p.getHealth()==p.getMaxHealth(),"No dead enemy retaliation");
        check(p.getXp()==10&&p.getCoins()==2,"Victory rewards");engine.performAction(CombatAction.ATTACK);
        check(p.getXp()==10&&p.getCoins()==2,"Rewards exactly once");
        p=new Warrior("Test");p.setHealth(1);p.gainXp(72);check(p.getLevel()==3&&p.getXp()==0,"Multi-level XP");check(p.getHealth()==p.getMaxHealth(),"Level heals");
        p.gainXp(Integer.MAX_VALUE);check(p.getLevel()==100&&p.getXp()==0,"XP cap without overflow");
        p=new Warrior("Test");p.setHealth(50);int potions=p.getPotions();check(p.usePotion()&&p.getHealth()==90&&p.getPotions()==potions-1,"Potion scaling");
        Player guarded=new Warrior("G"),plain=new Warrior("P");
        new CombatEngine(guarded,enemy(500,10),new Random(4)).performAction(CombatAction.DEFEND);
        new CombatEngine(plain,enemy(500,10),new Random(4)).performAction(CombatAction.POTION); // invalid; independently compare fixed damage
        check(guarded.getHealth()>=95,"Guard protects retaliation");
        p=new Warrior("Test");e=enemy(500,8);engine=new CombatEngine(p,e,new Random(1));e.applyStatus(new StatusEffect("stun",StatusEffect.Kind.STUN,0,1));
        engine.performAction(CombatAction.ATTACK);check(p.getHealth()==p.getMaxHealth()&&e.getStatuses().isEmpty(),"Stun skips one action");
        p=new Warrior("Test");e=enemy(1,0);engine=new CombatEngine(p,e,new Random(1));e.applyStatus(new StatusEffect("poison",StatusEffect.Kind.DAMAGE_OVER_TIME,2,1));
        check(engine.performAction(CombatAction.DEFEND).outcome()==CombatResult.Outcome.VICTORY,"DOT victory");
        p=new Warrior("Test");p.setHealth(1);engine=new CombatEngine(p,enemy(500,50),new Random(1));
        check(engine.performAction(CombatAction.ATTACK).outcome()==CombatResult.Outcome.DEFEAT,"Defeat");check(p.getXp()==0,"No defeat reward");
        p=new Rogue("Test");engine=new CombatEngine(p,enemy(500,50),new Random(){@Override public double nextDouble(){return 0;}});
        check(engine.performAction(CombatAction.FLEE).outcome()==CombatResult.Outcome.FLED&&p.getXp()==0,"Escape without rewards");
        for(PlayerClass type:PlayerClass.values()) {
            p=type.create("Roundtrip"+type);p.setLevel(5);p.setXp(7);p.setHealth(30);p.setResource(11);p.setCoins(9);p.setPotions(2);p.setSwordDamage(3);
            SaveManager.savePlayer(p);Player loaded=SaveManager.loadPlayer(p.getName());
            check(loaded!=null&&loaded.getPlayerClass()==type&&loaded.getLevel()==5&&loaded.getXp()==7&&loaded.getHealth()==30&&loaded.getResource()==11&&loaded.getCoins()==9&&loaded.getPotions()==2&&loaded.getSwordDamage()==3,"Save roundtrip "+type);
            Files.delete(Path.of("saves-v4",p.getName()+".txt"));
        }
        for(PlayerClass type:PlayerClass.values()) {
            p=type.create("Ability");p.setHealth(p.getMaxHealth()/2);e=enemy(500,0);engine=new CombatEngine(p,e,new Random(1));
            int before=p.getHealth();
            check(engine.performAction(CombatAction.ABILITY,p.getAbilities().get(0).id()).accepted(),"Class ability executes "+type);
            check(type==PlayerClass.CLERIC?p.getHealth()>before:e.getHealth()<500,"Class ability effect "+type);
        }
        p=new Warrior("Status");p.setHealth(50);p.applyStatus(new StatusEffect("regen",StatusEffect.Kind.REGENERATION,5,2));p.endTurn();p.endTurn();check(p.getHealth()==60,"Regeneration duration");
        p.applyStatus(new StatusEffect("burn",StatusEffect.Kind.DAMAGE_OVER_TIME,3,2));p.applyStatus(new StatusEffect("burn",StatusEffect.Kind.DAMAGE_OVER_TIME,3,2));p.endTurn();check(p.getHealth()==57,"Reapply refreshes without stacking");
        Files.createDirectories(Path.of("saves"));
        Path legacy=Path.of("saves","Legacy.txt");
        String old="Name=Legacy\nClass=Mage\nLevel=2\nXp=10\nXpToNextLevel=35\nHealth=12\nMaxHealth=24\nCoins=4\nPotions=1\nSwordDamage=2\n";
        Files.writeString(legacy,old);p=SaveManager.loadPlayer("Legacy");check(p!=null&&p.getMaxHealth()==90&&p.getHealth()==45&&p.getXp()==12,"Legacy save migration");check(Files.readString(legacy).equals(old),"Legacy unchanged");Files.delete(legacy);
        check(SaveManager.loadPlayer("../escape")==null,"Save path validation");
        for(int level:new int[]{1,5,10,16,25})for(PlayerClass type:PlayerClass.values()) {
            double total=0;int wins=0;
            for(int seed=0;seed<200;seed++) {
                p=type.create("Balance");p.setLevel(level);p.setHealth(p.getMaxHealth());
                e=new Enemy("Normal",level,28+6*(level-1),9+2*(level-1),4+level,0,0,0,0);
                engine=new CombatEngine(p,e,new Random(seed));int turns=0;
                while(engine.getOutcome()==CombatResult.Outcome.ACTIVE&&turns++<100)engine.performAction(CombatAction.ATTACK);
                if(engine.getOutcome()==CombatResult.Outcome.VICTORY)wins++;
                total+=1.0-p.getHealth()/(double)p.getMaxHealth();
            }
            double loss=total/200;check(wins==200,"Normal fight survival "+type+level);check(loss>=0.1&&loss<=0.35,"Normal damage budget "+type+level+": "+loss);
            System.out.printf("%s level %d: average HP cost %.1f%%, wins %d/200%n",type,level,100*loss,wins);
        }
        System.out.println("Passed "+checks+" checks.");
    }
}
