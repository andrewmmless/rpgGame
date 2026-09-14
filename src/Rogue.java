import java.util.List;
public final class Rogue extends Player {
    public Rogue(String name) { super(name, PlayerClass.ROGUE); }
    @Override public List<Ability> getAbilities() { return AbilityBook.ROGUE; }
    @Override public double getFleeChance() { return 0.85; }
}
