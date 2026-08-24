// ==========================================================
// ROGUE — extends Player
// ==========================================================
// Low health, high crit chance, best at fleeing fights.
// ==========================================================

public class Rogue extends Player {

    public Rogue(String name) {
        super(name, 20, 5, 2);
    }

    public void backstab(java.util.Random gen, Character enemy) {
        int critChance = gen.nextInt(100) + 1;
        int damage = rollDamage(gen, 4, 9);
        if (critChance <= 30) {
            damage *= 2;
            System.out.println(name + " lands a critical Backstab!");
        } else {
            System.out.println(name + " backstabs " + enemy.getName() + ".");
        }
        enemy.takeDamage(damage);
    }

    // Rogue has a better flee chance than other classes (50% vs 30%)
    public boolean tryFlee(java.util.Random gen) {
        int chance = gen.nextInt(100) + 1;
        return chance <= 50;
    }
}