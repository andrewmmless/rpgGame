import java.util.List;
public final class Cleric extends Player {
    public Cleric(String name) { super(name, PlayerClass.CLERIC); }
    @Override public List<Ability> getAbilities() { return AbilityBook.CLERIC; }

}
