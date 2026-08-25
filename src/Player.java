// ==========================================================
// PLAYER — extends Character
// ==========================================================
// Inherits name/health/attackPower/defence/takeDamage()/isDead()
// from Character automatically. Only adds what's UNIQUE to a
// player: coins, potions, sword upgrades.
//
// Warrior/Mage/Cleric/Rogue each extend THIS class, so they get
// coins/potions too, plus their own special flavor on top.
// ==========================================================

public class Player extends Character {

    protected int coins;
    protected int potions;
    protected int swordDamage;

    public Player(String name, int health, int attackPower, int defence) {
        super(name, health, attackPower, defence);

        this.coins = 0;
        this.potions = 0;
        this.swordDamage = 0;
    }


    // ==========================================================
    // COIN MANAGEMENT
    // ==========================================================

    public void addCoins(int amount) {
        coins += amount;
    }

    public boolean spendCoins(int amount) {

        if (amount > coins) {
            System.out.println(
                    "Not enough coins! You have " + coins + "."
            );
            return false;
        }

        coins -= amount;
        return true;
    }

    public int getCoins() {
        return coins;
    }

    // Used when loading a saved game
    public void setCoins(int coins) {
        this.coins = coins;
    }


    // ==========================================================
    // POTIONS
    // ==========================================================

    public void addPotion() {
        potions++;
    }

    public boolean usePotion() {

        if (potions <= 0) {
            System.out.println("You have no potions.");
            return false;
        }

        potions--;

        heal(10);

        System.out.println(
                "Used a potion. Health: "
                        + health + "/" + maxHealth
        );

        return true;
    }

    public int getPotions() {
        return potions;
    }

    // Used when loading a saved game
    public void setPotions(int potions) {
        this.potions = potions;
    }


    // ==========================================================
    // SWORD UPGRADES
    // ==========================================================

    public void upgradeSword(int amount) {
        swordDamage += amount;
    }

    public int getSwordDamage() {
        return swordDamage;
    }

    // Used when loading a saved game
    public void setSwordDamage(int swordDamage) {
        this.swordDamage = swordDamage;
    }


    // ==========================================================
    // SAVING / LOADING HEALTH AND STATS
    // ==========================================================
    // These methods allow SaveManager to restore the player's
    // stats when loading a saved game.
    // ==========================================================

    public void setHealth(int health) {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }


    // ==========================================================
    // PLAYER ATTACK
    // ==========================================================
    // Player's attack includes their sword bonus on top of the
    // base roll from Character.
    // ==========================================================

    @Override
    public int rollDamage(java.util.Random gen, int min, int max) {
        return super.rollDamage(gen, min, max) + swordDamage;
    }
}