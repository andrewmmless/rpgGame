import java.util.Random;
import java.util.Scanner;

// ==========================================================
// FIGHT
// ==========================================================
// ONE battle loop, reused for every enemy type. This replaces
// the three separate ~100-line copy-pasted fight loops from
// the original procedural version.
//
// It checks the player's actual class (instanceof) to offer
// that class's special move, on top of a basic attack every
// class has.
// ==========================================================

public class Fight {

    public static void start(Scanner input, Random gen, Player player, Enemy enemy) {
        System.out.println("You encountered a Lv." + enemy.getLevel() + " " + enemy.getName() + "!");
        System.out.println(enemy.getName() + " - Health: " + enemy.getHealth()
                + " Attack: " + enemy.getAttackPower() + " Defence: " + enemy.getDefence());

        while (!player.isDead() && !enemy.isDead()) {
            System.out.println();
            System.out.println("Choose 'attack', 'special', 'heal', or 'run'.");
            String choice = input.nextLine();

            if (choice.equalsIgnoreCase("run")) {
                boolean fled = attemptFlee(gen, player);
                if (fled) {
                    System.out.println("You successfully fled!");
                    return;
                } else {
                    System.out.println("You failed to flee!");
                    enemy.takeDamage(0); // no damage, just wastes the turn
                    enemyTurn(gen, enemy, player);
                }

            } else if (choice.equalsIgnoreCase("attack")) {
                int damage = player.rollDamage(gen, 3, 8);
                System.out.println(player.getName() + " attacks " + enemy.getName() + "!");
                enemy.takeDamage(damage);
                if (!enemy.isDead()) enemyTurn(gen, enemy, player);

            } else if (choice.equalsIgnoreCase("special")) {
                useSpecial(gen, player, enemy);
                if (!enemy.isDead()) enemyTurn(gen, enemy, player);

            } else if (choice.equalsIgnoreCase("heal")) {
                player.usePotion();
                enemyTurn(gen, enemy, player);

            } else {
                System.out.println("Invalid choice.");
                continue;
            }

            if (enemy.isDead()) {
                int coinsGained = enemy.rollCoinReward(gen);
                int xpGained = enemy.rollXpReward(gen);
                player.addCoins(coinsGained);
                System.out.println("Defeated " + enemy.getName() + "! Gained " + coinsGained + " coins.");
                player.gainXp(xpGained);
            }

            if (player.isDead()) {
                System.out.println(player.getName() + " has died...");
            }
        }
    }

    // Each class's special move — this is where "vary for classes" happens
    private static void useSpecial(Random gen, Player player, Enemy enemy) {
        if (player instanceof Warrior warrior) {
            warrior.koSlash(gen, enemy);
        } else if (player instanceof Mage mage) {
            mage.fireball(gen, enemy);
        } else if (player instanceof Cleric cleric) {
            cleric.prayer(); // Cleric's "special" is healing instead of damage
        } else if (player instanceof Rogue rogue) {
            rogue.backstab(gen, enemy);
        } else {
            System.out.println("This class has no special move.");
        }
    }

    private static boolean attemptFlee(Random gen, Player player) {
        int chance = gen.nextInt(100) + 1;
        int fleeThreshold = (player instanceof Rogue) ? 50 : 30; // Rogue flees more often
        return chance <= fleeThreshold;
    }

    private static void enemyTurn(Random gen, Enemy enemy, Player player) {
        int damage = enemy.rollDamage(gen, 1, 6);
        System.out.println(enemy.getName() + " attacks " + player.getName() + "!");
        player.takeDamage(damage);
    }
}