import java.util.Random;
import java.util.Scanner;

// ==========================================================
// MAIN
// ==========================================================
// Notice how thin this is compared to the original v1/v2.
// Main doesn't contain any game LOGIC anymore — it just
// creates objects and calls their methods. All the actual
// rules live inside the classes that own the relevant data.
// ==========================================================

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Random gen = new Random();

        System.out.println("Welcome to Andrew's adventure game v3 (OOP edition)");
        System.out.println("Choose your class: Warrior, Mage, Cleric, Rogue");
        String classChoice = input.nextLine();

        System.out.println("Enter your character's name:");
        String playerName = input.nextLine();

        Player player = createPlayer(classChoice, playerName);
        if (player == null) {
            System.out.println("That class is not available.");
            return;
        }

        System.out.println("Welcome, " + player.getName() + " the " + classChoice + "!");

        boolean playing = true;
        while (playing && !player.isDead()) {
            System.out.println();
            System.out.println("You have " + player.getCoins() + " coins and "
                    + player.getHealth() + "/" + player.getMaxHealth() + " health.");
            System.out.println("What would you like to do? (Fight, Shop, Heal, Coke, Quit)");
            String choice = input.nextLine();

            if (choice.equalsIgnoreCase("fight")) {
                Enemy enemy = EnemyFactory.randomEnemy(gen);
                Fight.start(input, gen, player, enemy);

            } else if (choice.equalsIgnoreCase("shop")) {
                Shop.enter(input, player);

            } else if (choice.equalsIgnoreCase("heal")) {
                player.usePotion();

            } else if (choice.equalsIgnoreCase("coke")) {
                Items.drinkCoke(gen, player);

            } else if (choice.equalsIgnoreCase("quit")) {
                playing = false;

            } else {
                System.out.println("Invalid choice.");
            }
        }

        if (player.isDead()) {
            System.out.println("=====================");
            System.out.println("GAME OVER");
            System.out.println("=====================");
        } else {
            System.out.println("Thanks for playing!");
        }

        input.close();
    }

    // Central place that turns a typed class name into the right object.
    // Adding a 5th class later = one more "else if" here, nothing else changes.
    private static Player createPlayer(String classChoice, String name) {
        if (classChoice.equalsIgnoreCase("Warrior")) {
            return new Warrior(name);
        } else if (classChoice.equalsIgnoreCase("Mage")) {
            return new Mage(name);
        } else if (classChoice.equalsIgnoreCase("Cleric")) {
            return new Cleric(name);
        } else if (classChoice.equalsIgnoreCase("Rogue")) {
            return new Rogue(name);
        } else {
            return null;
        }
    }
}