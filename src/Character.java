import java.util.*;

public abstract class Character {
    protected final String name;
    protected int health, maxHealth, attackPower, defence;
    private final Map<String, ActiveStatus> statuses = new LinkedHashMap<>();
    private final EnumMap<DamageType, Double> resistances = new EnumMap<>(DamageType.class);
    private static final class ActiveStatus {
        final StatusEffect effect; int remaining;
        ActiveStatus(StatusEffect effect) { this.effect=effect; remaining=effect.duration(); }
    }
    protected Character(String name, int health, int attackPower, int defence) {
        if (name == null || name.isBlank() || health < 1 || attackPower < 0 || defence < 0)
            throw new IllegalArgumentException("Invalid character");
        this.name=name; this.health=health; maxHealth=health; this.attackPower=attackPower; this.defence=defence;
    }
    public void takeDamage(int amount) { receiveDamage(amount, DamageType.PHYSICAL); }
    public int receiveDamage(int amount, DamageType type) {
        Objects.requireNonNull(type);
        int damage=Balance.damage(amount, getDefence(), type);
        if (type != DamageType.TRUE) damage=(int)Math.round(damage * (1-resistances.getOrDefault(type, 0.0)));
        if (hasStatus(StatusEffect.Kind.GUARD)) damage=(int)Math.ceil(damage * 0.5);
        int dealt=Math.min(health, damage); health-=dealt; return dealt;
    }
    public void setResistance(DamageType type, double resistance) {
        if (!Double.isFinite(resistance) || resistance < -1 || resistance > 0.8 || type == DamageType.TRUE)
            throw new IllegalArgumentException("Resistance must be -1..0.8 and not TRUE");
        resistances.put(Objects.requireNonNull(type), resistance);
    }
    public void heal(int amount) { if (amount < 0) throw new IllegalArgumentException("Negative healing"); health=(int)Math.min(maxHealth,(long)health+amount); }
    public boolean isDead() { return health == 0; }
    public int rollDamage(Random random, int min, int max) { return random.nextInt(max-min+1)+min+attackPower; }
    public void applyStatus(StatusEffect effect) { statuses.put(effect.id(), new ActiveStatus(effect)); }
    public boolean hasStatus(StatusEffect.Kind kind) { return statuses.values().stream().anyMatch(s -> s.effect.kind()==kind); }
    /** Tick once at the end of the affected actor's turn. Reapplication refreshes; never stacks. */
    public void endTurn() {
        for (var it=statuses.values().iterator(); it.hasNext();) {
            ActiveStatus s=it.next();
            if (!isDead()) switch(s.effect.kind()) {
                case DAMAGE_OVER_TIME -> receiveDamage(s.effect.potency(), DamageType.TRUE);
                case REGENERATION -> heal(s.effect.potency());
                default -> { }
            }
            if (--s.remaining == 0) it.remove();
        }
    }
    public record StatusState(StatusEffect effect, int remaining) {}
    public List<StatusState> snapshotStatuses() {
        return statuses.values().stream().map(s -> new StatusState(s.effect, s.remaining)).toList();
    }
    public void restoreStatuses(List<StatusState> saved) {
        statuses.clear();
        for (StatusState state : saved) {
            if (state.remaining() < 1 || state.remaining() > state.effect().duration())
                throw new IllegalArgumentException("Invalid status duration");
            ActiveStatus active = new ActiveStatus(state.effect());
            active.remaining = state.remaining();
            statuses.put(state.effect().id(), active);
        }
    }
    public void removeStatus(String id){statuses.remove(id);}
    public void clearStatuses() { statuses.clear(); }
    public Map<String,Integer> getStatuses() {
        Map<String,Integer> result=new LinkedHashMap<>(); statuses.forEach((id,s)->result.put(id,s.remaining)); return Map.copyOf(result);
    }
    public String getName(){return name;} public int getHealth(){return health;}
    public int getMaxHealth(){return maxHealth;} public int getAttackPower(){return attackPower;}
    public int getDefence(){return defence;}
}
