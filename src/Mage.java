import java.util.List;
public final class Mage extends Player {
    public Mage(String name) { super(name, PlayerClass.MAGE); }
    @Override public List<Ability> getAbilities() { return AbilityBook.MAGE; }

}
