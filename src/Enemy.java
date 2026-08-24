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
// ==========================================================

public class Enemy extends Character {

    private int coinRewardMin;
    private int coinRewardMax;

    public Enemy(String name, int health, int attackPower, int defence,
                 int coinRewardMin, int coinRewardMax) {
        super(name, health, attackPower, defence);
        this.coinRewardMin = coinRewardMin;
        this.coinRewardMax = coinRewardMax;
    }

    public int rollCoinReward(java.util.Random gen) {
        return gen.nextInt(coinRewardMax - coinRewardMin + 1) + coinRewardMin;
    }
}