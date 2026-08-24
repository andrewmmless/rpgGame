import java.util.Random;

// ==========================================================
// ITEMS
// ==========================================================
// Small standalone actions that affect a Player, kept out of
// Main to keep the menu loop thin. Same pattern as Shop.
// ==========================================================

public class Items {

    // 50/50: full heal, or lose a big chunk of health
    public static void drinkCoke(Random gen, Player player) {
        int roll = gen.nextInt(2) + 1;
        if (roll == 1) {
            System.out.println("Ouuuu yikes. You took cocaine instead of coke.");
            System.out.println(player.getName() + " loses 15 health!");
            player.takeDamage(15);
        } else {
            System.out.println("You drank a refreshing coke and feel great!");
            player.heal(player.getMaxHealth()); // full heal
            System.out.println("Health: " + player.getHealth() + "/" + player.getMaxHealth());
        }
    }
}