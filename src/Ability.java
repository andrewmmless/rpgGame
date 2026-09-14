import java.util.*;
/** Data and behavior are composed; the engine does not need to know a player's class. */
public record Ability(String id, String name, int cost, int cooldown, int unlockLevel, Effect effect) {
    @FunctionalInterface public interface Effect { void apply(Player user, Character target, Random random, List<String> events); }
    public Ability {
        Objects.requireNonNull(id); Objects.requireNonNull(name); Objects.requireNonNull(effect);
        if (id.isBlank() || name.isBlank() || cost<0 || cooldown<0 || unlockLevel<1) throw new IllegalArgumentException("Invalid ability");
    }
    public static Ability strike(String id, String name, int cost, int cooldown, double multiplier, DamageType type, StatusEffect.Kind status) {
        return new Ability(id,name,cost,cooldown,1,(user,target,random,events)-> {
            int raw=(int)Math.round(user.rollDamage(random,-2,2)*multiplier);
            events.add(name+" dealt "+target.receiveDamage(raw,type)+" damage.");
            if (status != null && !target.isDead()) target.applyStatus(new StatusEffect(id,status,Math.max(2,user.getLevel()+1),status == StatusEffect.Kind.STUN ? 1 : 2));
        });
    }
}
