import java.util.List;
public final class Mage extends Player {
    private static final List<Ability> ABILITIES=List.of(Ability.strike("fireball","Fireball",14,2,1.5,DamageType.FIRE,StatusEffect.Kind.DAMAGE_OVER_TIME));
    public Mage(String name){super(name,PlayerClass.MAGE);}
    @Override public List<Ability> getAbilities(){return ABILITIES;}

}
