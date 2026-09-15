/** Shared tuning rules. XP targets roughly five early encounters per level, increasing gradually. */
public final class Balance {
    private Balance() {}
    public static final int MAX_LEVEL = 100;
    public static int xpNeeded(int level) { return 50 + 20 * (level - 1) + (level - 1) * (level - 1) / 2; }
    public static int enemyXp(int level) { return 10 + 4 * (level - 1); }
    public static int damage(int raw, int defence, DamageType type) {
        if (raw <= 0) return 0;
        return Math.max(1, (int)Math.round(raw * (type == DamageType.TRUE ? 1.0 : 50.0 / (50 + defence))));
    }
}
