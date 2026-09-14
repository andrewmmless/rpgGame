import java.util.List;
public record CombatResult(boolean accepted, Outcome outcome, List<String> events, int playerHealth, int enemyHealth) {
    public enum Outcome { ACTIVE, VICTORY, DEFEAT, FLED }
    public CombatResult { events=List.copyOf(events); }
}
