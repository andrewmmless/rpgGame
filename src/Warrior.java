import java.util.List;
public final class Warrior extends Player {
    private static final List<Ability> ABILITIES=List.of(Ability.strike("ko_slash","KO Slash",12,2,1.7,DamageType.PHYSICAL,null));
    public Warrior(String name){super(name,PlayerClass.WARRIOR);}
    @Override public List<Ability> getAbilities(){return ABILITIES;}

}
