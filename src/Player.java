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
        super(name, health, attackPower, defence); // calls Character's constructor
        this.coins = 0;
        this.potions = 0;
        this.swordDamage = 0;
    }

    // ---- coin management ----

    public void addCoins(int amount) {
        coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (amount > coins) {
            System.out.println("Not enough coins! You have " + coins + ".");
            return false;
        }
        coins -= amount;
        return true;
    }

    public int getCoins() {
        return coins;
    }

    // ---- potions ----

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
        System.out.println("Used a potion. Health: " + health + "/" + maxHealth);
        return true;
    }

    public int getPotions() {
        return potions;
    }

    // ---- sword upgrades ----

    public void upgradeSword(int amount) {
        swordDamage += amount;
    }

    public int getSwordDamage() {
        return swordDamage;
    }

    // Player's attack includes their sword bonus on top of the base roll
    @Override
    public int rollDamage(java.util.Random gen, int min, int max) {
        return super.rollDamage(gen, min, max) + swordDamage;
    }
}