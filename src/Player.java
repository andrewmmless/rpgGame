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

    protected int level;
    protected int xp;
    protected int xpToNextLevel;

    public Player(String name, int health, int attackPower, int defence) {
        super(name, health, attackPower, defence);

        this.coins = 0;
        this.potions = 0;
        this.swordDamage = 0;

        this.level = 1;
        this.xp = 0;
        this.xpToNextLevel = xpNeededFor(level);
    }


    // ==========================================================
    // LEVELING
    // ==========================================================
    // gainXp() is what Fight calls after a win. It adds XP, then
    // loops levelUp() in case one kill provides enough XP to
    // clear more than one level at once (e.g. a mini-boss kill).
    // ==========================================================

    public void gainXp(int amount) {
        xp += amount;
        System.out.println(name + " gained " + amount + " XP.");

        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            levelUp();
        }
    }

    // How much XP is needed to clear a given level. Grows each
    // level so higher levels take progressively longer to reach.
    protected int xpNeededFor(int atLevel) {
        return 20 + (atLevel - 1) * 15;
    }

    // Subclasses (like Mage, for mana) can override this to add
    // their own bonus on top, as long as they call super.levelUp().
    protected void levelUp() {
        level++;

        maxHealth += 6;
        attackPower += 2;
        defence += 1;
        health = maxHealth; // level up fully restores health

        xpToNextLevel = xpNeededFor(level);

        System.out.println();
        System.out.println(name + " leveled up! Now level " + level + ".");
        System.out.println("Max Health: " + maxHealth
                + " | Attack: " + attackPower
                + " | Defence: " + defence);
    }

    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }

    // Used when loading a saved game
    public void setLevel(int level) { this.level = level; }
    public void setXp(int xp) { this.xp = xp; }
    public void setXpToNextLevel(int xpToNextLevel) { this.xpToNextLevel = xpToNextLevel; }


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