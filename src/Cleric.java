import java.util.List;
public final class Cleric extends Player {
    private static final List<Ability> ABILITIES=List.of(new Ability("prayer","Prayer",14,3,1,(user,target,random,events)->{int before=user.getHealth();user.heal(user.getMaxHealth()*30/100);events.add("Prayer restored "+(user.getHealth()-before)+" health.");}));
    public Cleric(String name){super(name,PlayerClass.CLERIC);}
    @Override public List<Ability> getAbilities(){return ABILITIES;}

}
