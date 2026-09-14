import java.util.List;

/** A small, distinct progression for each original class. */
public final class AbilityBook {
    private AbilityBook() {}
    private static Ability at(int level, Ability a) {
        return new Ability(a.id(), a.name(), a.cost(), a.cooldown(), level, a.effect());
    }
    private static Ability recover(String id, String name, int cost, int cooldown, int level, int percent) {
        return new Ability(id, name, cost, cooldown, level, (user, target, random, events) -> {
            int before = user.getHealth();
            user.heal(user.getMaxHealth() * percent / 100);
            events.add(name + " restored " + (user.getHealth() - before) + " health.");
        });
    }
    public static final List<Ability> WARRIOR = List.of(
        Ability.strike("ko_slash", "KO Slash", 12, 2, 1.7, DamageType.PHYSICAL, null),
        at(2, Ability.strike("shield_bash", "Shield Bash", 14, 3, 0.8, DamageType.PHYSICAL, StatusEffect.Kind.STUN)),
        recover("second_wind", "Second Wind", 18, 4, 5, 25),
        at(8, Ability.strike("execution", "Execution", 24, 4, 2.6, DamageType.PHYSICAL, null)));
    public static final List<Ability> MAGE = List.of(
        Ability.strike("fireball", "Fireball", 14, 2, 1.5, DamageType.FIRE, StatusEffect.Kind.DAMAGE_OVER_TIME),
        at(2, Ability.strike("frostbolt", "Frostbolt", 16, 3, 0.8, DamageType.ICE, StatusEffect.Kind.STUN)),
        new Ability("arcane_focus", "Arcane Focus", 0, 4, 5, (u,t,r,e) -> {u.restoreResource(22); e.add("Recovered 22 resource.");}),
        at(8, Ability.strike("meteor", "Meteor", 26, 4, 2.5, DamageType.FIRE, StatusEffect.Kind.DAMAGE_OVER_TIME)));
    public static final List<Ability> CLERIC = List.of(
        recover("prayer", "Prayer", 14, 3, 1, 30),
        at(2, Ability.strike("smite", "Smite", 12, 2, 1.8, DamageType.HOLY, null)),
        new Ability("renew", "Renew", 18, 4, 5, (u,t,r,e) -> {
            u.applyStatus(new StatusEffect("renew", StatusEffect.Kind.REGENERATION, u.getMaxHealth()/10, 3));
            e.add("Renew restores health over three turns.");
        }),
        at(8, Ability.strike("judgement", "Judgement", 24, 4, 2.5, DamageType.HOLY, null)));
    public static final List<Ability> ROGUE = List.of(
        Ability.strike("backstab", "Backstab", 12, 2, 1.6, DamageType.PHYSICAL, StatusEffect.Kind.DAMAGE_OVER_TIME),
        at(2, Ability.strike("pocket_sand", "Pocket Sand", 12, 3, 0.7, DamageType.PHYSICAL, StatusEffect.Kind.STUN)),
        at(5, Ability.strike("venom", "Venom Strike", 16, 3, 1.8, DamageType.POISON, StatusEffect.Kind.DAMAGE_OVER_TIME)),
        at(8, Ability.strike("deathmark", "Deathmark", 24, 4, 2.6, DamageType.PHYSICAL, null)));
}
