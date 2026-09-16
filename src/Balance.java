/** Shared tuning rules. XP targets roughly five early encounters per level, increasing gradually. */
public final class Balance {
    private Balance() {}
    public static final int MAX_LEVEL = 100;
    public static int xpNeeded(int level) { return 50 + 20 * (level - 1) + (level - 1) * (level - 1) / 2; }
    public static int storyClearXp(int level,int xp,int route){
        SubArea path=SubArea.get(route);int target=route==11?60:SubArea.get(route+1).minLevel();int gap=-xp;
        for(int n=level;n<target;n++)gap+=xpNeeded(n);
        return Math.max(xpNeeded(path.minLevel())*2,Math.max(0,gap));
    }
    public static int enemyXp(int level) { return 10 + 4 * (level - 1); }
    public static int damage(int raw, int defence, DamageType type) {
        if (raw <= 0) return 0;
        return Math.max(1, (int)Math.round(raw * (type == DamageType.TRUE ? 1.0 : 50.0 / (50 + defence))));
    }
}
