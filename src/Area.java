// ==========================================================
// AREA — the zones the player can fight in
// ==========================================================
// Each area has a recommended level range. EnemyFactory uses
// this to decide which enemies (and what stats) to hand out.
// Adding a new zone = adding one line here + one case in
// EnemyFactory. No new classes needed.
// ==========================================================

public enum Area {

    WHISPERING_WOODS("Whispering Woods", 1, 4),
    STONEFANG_CAVES("Stonefang Caves", 5, 9),
    FORGOTTEN_RUINS("Forgotten Ruins", 10, 15),
    DRAGONS_SPIRE("Dragon's Spire", 16, 25);

    private final String displayName;
    private final int minLevel;
    private final int maxLevel;

    Area(String displayName, int minLevel, int maxLevel) {
        this.displayName = displayName;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public String getDisplayName() { return displayName; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }

    // Picks whichever area's range best fits the player's current level.
    // Used to suggest a starting point, not to block anything.
    public static Area recommendedFor(int playerLevel) {
        Area best = WHISPERING_WOODS;
        for (Area area : values()) {
            if (playerLevel >= area.minLevel) {
                best = area;
            }
        }
        return best;
    }
}
