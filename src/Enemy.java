// ==========================================================
// ENEMY — extends Character
// ==========================================================
// DESIGN NOTE: Goblin/Wolf/Victoria are NOT separate subclasses
// here. They're just different OBJECTS built from this one
// Enemy class, each with their own stats passed into the
// constructor. This is the key lesson from earlier: you don't
// need a new class for every enemy, just a new object.
//
// (Subclassing is for when behavior genuinely differs, like
// Mage having mana. Enemies here only differ in numbers, so
// plain instantiation is the right tool, not inheritance.)
//
// "level" is just a display/balance label (what tier of area
// this enemy belongs to) — it doesn't change any formulas.
// It's the coin/xp rewards and raw stats that actually matter.
// ==========================================================

public class Enemy extends Character {

    private int level;
    private int coinRewardMin;
    private int coinRewardMax;
    private int xpRewardMin;
    private int xpRewardMax;

    public Enemy(String name, int level, int health, int attackPower, int defence,
                 int coinRewardMin, int coinRewardMax,
                 int xpRewardMin, int xpRewardMax) {
        super(name, health, attackPower, defence);
        this.level = level;
        this.coinRewardMin = coinRewardMin;
        this.coinRewardMax = coinRewardMax;
        this.xpRewardMin = xpRewardMin;
        this.xpRewardMax = xpRewardMax;
    }

    public int rollCoinReward(java.util.Random gen) {
        return gen.nextInt(coinRewardMax - coinRewardMin + 1) + coinRewardMin;
    }

    public int rollXpReward(java.util.Random gen) {
        return gen.nextInt(xpRewardMax - xpRewardMin + 1) + xpRewardMin;
    }

    public int getLevel() { return level; }
}
