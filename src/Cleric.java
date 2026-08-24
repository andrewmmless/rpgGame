// ==========================================================
// CLERIC — extends Player
// ==========================================================
// Balanced stats, unique ability: can heal without a potion.
// ==========================================================

public class Cleric extends Player {

    public Cleric(String name) {
        super(name, 24, 4, 3);
    }

    public void prayer() {
        int healAmount = 12;
        heal(healAmount); // heal() is inherited from Character
        System.out.println(name + " prays and recovers " + healAmount + " health. Health: " + health + "/" + maxHealth);
    }
}