import java.util.Random;

// ==========================================================
// CHARACTER — the shared parent class
// ==========================================================
// Both Player and Enemy "extend" this class, meaning they
// automatically inherit everything below without retyping it.
//
// "protected" (instead of private) is what lets Player and
// Enemy reach into these fields directly, since they're
// child classes — outside classes like Main still cannot.
// ==========================================================

public class Character {

    protected String name;
    protected int health;
    protected int maxHealth;
    protected int attackPower;
    protected int defence;

    public Character(String name, int health, int attackPower, int defence) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.attackPower = attackPower;
        this.defence = defence;
    }

    // ---- shared behavior every Character has ----

    public void takeDamage(int amount) {
        // defence reduces incoming damage, minimum 1 damage always gets through
        int reduced = Math.max(1, amount - defence);
        health -= reduced;
        if (health < 0) health = 0;
        System.out.println(name + " took " + reduced + " damage. Health: " + health + "/" + maxHealth);
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    // Basic attack roll every Character can do — subclasses can call this
    // or define their own special moves on top of it.
    public int rollDamage(Random gen, int min, int max) {
        return gen.nextInt(max - min + 1) + min + attackPower;
    }

    // ---- getters (health/name are protected, but outside classes
    //      like Main still need a safe way to read them) ----

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttackPower() { return attackPower; }
    public int getDefence() { return defence; }
}