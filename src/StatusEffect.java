import java.util.Objects;
/** Immutable reusable definition; each target owns its independent remaining duration. */
public record StatusEffect(String id, Kind kind, int potency, int duration) {
    public enum Kind { DAMAGE_OVER_TIME, REGENERATION, STUN, GUARD }
    public StatusEffect {
        Objects.requireNonNull(id); Objects.requireNonNull(kind);
        if (id.isBlank() || potency < 0 || duration < 1) throw new IllegalArgumentException("Invalid status");
    }
}
