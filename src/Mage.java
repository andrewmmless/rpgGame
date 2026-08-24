// ==========================================================
// MAGE — extends Player
// ==========================================================
// Lower health, but has a resource (mana) nothing else has.
// This shows why inheritance is useful: Mage adds a field
// (mana) that Warrior/Cleric/Rogue don't need at all, without
// forcing Character or Player to carry it around unused.
// ==========================================================

public class Mage extends Player {

    private int mana;
    private int maxMana;

    public Mage(String name) {
        super(name, 18, 3, 1); // low health/defence, weak base attack
        this.mana = 20;
        this.maxMana = 20;
    }

    public void fireball(java.util.Random gen, Character enemy) {
        int cost = 8;
        if (mana < cost) {
            System.out.println("Not enough mana for Fireball!");
            return;
        }
        mana -= cost;
        int damage = rollDamage(gen, 10, 20);
        System.out.println(name + " casts Fireball!");
        enemy.takeDamage(damage);
    }

    public void restoreMana(int amount) {
        mana += amount;
        if (mana > maxMana) mana = maxMana;
    }

    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
}