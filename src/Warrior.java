// ==========================================================
// WARRIOR — extends Player
// ==========================================================
// High health/defence, no special resource. Simple, sturdy.
// ==========================================================

public class Warrior extends Player {

    public Warrior(String name) {
        super(name, 30, 6, 4); // health, attackPower, defence
    }

    // Warrior's signature move: KO Slash — big hit, chance to miss
    public void koSlash(java.util.Random gen, Character enemy) {
        int hitChance = gen.nextInt(100) + 1;
        if (hitChance <= 55) {
            int damage = rollDamage(gen, 8, 16);
            System.out.println(name + " lands a KO Slash!");
            enemy.takeDamage(damage);
        } else {
            System.out.println(name + "'s KO Slash misses!");
        }
    }
}