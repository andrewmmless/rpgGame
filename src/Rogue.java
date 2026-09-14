import java.util.List;
public final class Rogue extends Player {
    private static final List<Ability> ABILITIES=List.of(Ability.strike("backstab","Backstab",12,2,1.6,DamageType.PHYSICAL,StatusEffect.Kind.DAMAGE_OVER_TIME));
    public Rogue(String name){super(name,PlayerClass.ROGUE);}
    @Override public List<Ability> getAbilities(){return ABILITIES;}
    @Override public double getFleeChance(){return 0.85;}
}
