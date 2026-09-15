import java.util.*;
/** Mixed-class combinations require coordinated actions and actual damage. */
public final class DuoCombos {
    public static boolean eligible(Player[] players,String first,String second){return players[0].getPlayerClass()!=players[1].getPlayerClass()&&((first.startsWith("ability:")&&second.startsWith("ability:"))||(first.equals("PROTECT")&&second.startsWith("ability:"))||(second.equals("PROTECT")&&first.startsWith("ability:")));}
    public static List<String> apply(Player[] players,Enemy enemy){
        Set<PlayerClass> classes=EnumSet.of(players[0].getPlayerClass(),players[1].getPlayerClass());String name;
        int raw=Math.max(2,(players[0].getAttackPower()+players[1].getAttackPower())/10);List<String> events=new ArrayList<>();
        if(classes.contains(PlayerClass.CLERIC)){name=classes.contains(PlayerClass.WARRIOR)?"Oathkeepers":classes.contains(PlayerClass.MAGE)?"Dawnfire":"Hidden Mercy";for(Player p:players)if(!p.isDead())p.heal(Math.max(2,p.getMaxHealth()/12));events.add(name+": the company restores 8% health.");}
        else if(classes.contains(PlayerClass.WARRIOR)){name=classes.contains(PlayerClass.MAGE)?"Shielded Inferno":"Flanking Guard";for(Player p:players)p.applyStatus(new StatusEffect("duo_guard",StatusEffect.Kind.GUARD,0,1));events.add(name+": both partners guard the incoming attack.");}
        else{name="Smoke and Cinders";for(Player p:players)p.restoreResource(6);events.add(name+": both partners recover 6 resource.");}
        if(!enemy.isDead())events.add("Combo strike deals "+enemy.receiveDamage(raw,DamageType.TRUE)+" damage.");return events;
    }
}
