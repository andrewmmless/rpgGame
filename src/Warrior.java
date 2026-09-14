import java.util.List;
public final class Warrior extends Player {
    public Warrior(String name) { super(name, PlayerClass.WARRIOR); }
    @Override public List<Ability> getAbilities() { return AbilityBook.WARRIOR; }

}
