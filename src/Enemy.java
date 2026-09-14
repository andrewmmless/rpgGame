/** Encounter stats and reward ranges, independent of presentation. */
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
        if(level<1 || level>Balance.MAX_LEVEL || coinRewardMin<0 || coinRewardMax<coinRewardMin || xpRewardMin<0 || xpRewardMax<xpRewardMin)
            throw new IllegalArgumentException("Invalid enemy rewards or level");
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
