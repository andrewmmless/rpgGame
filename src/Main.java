// ==========================================================
// MAIN
// ==========================================================
// Main handles the overall flow of the game.
//
// SaveManager handles:
// - Saving
// - Loading
// - Deleting saves
//
// Main does not contain the actual game logic for fighting,
// shopping, healing, etc. Those jobs belong to the other
// classes.
// ==========================================================

import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        Random gen = new Random();

        System.out.println(
                "Welcome to Andrew's adventure game v3 (OOP edition)"
        );

        System.out.println();


        // ==========================================================
        // START MENU
        // ==========================================================

        System.out.println("1. New Game");
        System.out.println("2. Load Game");
        System.out.println("3. Delete Save");
        System.out.println("4. Quit");
        System.out.println();

        System.out.print("Choose an option: ");

        String menuChoice = input.nextLine();

        Player player = null;


        // ==========================================================
        // NEW GAME
        // ==========================================================

        if (menuChoice.equals("1")) {

            System.out.println();

            System.out.println(
                    "Choose your class: Warrior, Mage, Cleric, Rogue"
            );

            String classChoice = input.nextLine();

            System.out.println();

            System.out.println("Enter your character's name:");

            String playerName = input.nextLine();


            // Create the correct Player object
            player = createPlayer(classChoice, playerName);


            // Check if the class was valid
            if (player == null) {

                System.out.println(
                        "That class is not available."
                );

                input.close();
                return;
            }


            System.out.println();

            System.out.println(
                    "Welcome, " + player.getName()
                            + " the " + classChoice + "!"
            );
        }


        // ==========================================================
        // LOAD GAME
        // ==========================================================

        else if (menuChoice.equals("2")) {

            System.out.println();

            System.out.println(
                    "Enter the name of the character you want to load:"
            );

            String playerName = input.nextLine();


            // Load the player from the save file
            player = SaveManager.loadPlayer(playerName);


            // If loading failed, go back to the menu/program
            if (player == null) {

                input.close();
                return;
            }


            System.out.println();

            System.out.println(
                    "Welcome back, " + player.getName() + "!"
            );
        }


        // ==========================================================
        // DELETE SAVE
        // ==========================================================

        else if (menuChoice.equals("3")) {

            System.out.println();

            System.out.println(
                    "Enter the name of the save you want to delete:"
            );

            String playerName = input.nextLine();


            // Delete the save
            SaveManager.deleteSave(playerName);


            input.close();
            return;
        }


        // ==========================================================
        // QUIT
        // ==========================================================

        else if (menuChoice.equals("4")) {

            System.out.println("Goodbye!");

            input.close();
            return;
        }


        // ==========================================================
        // INVALID MENU CHOICE
        // ==========================================================

        else {

            System.out.println("Invalid choice.");

            input.close();
            return;
        }


        // ==========================================================
        // GAME LOOP
        // ==========================================================

        boolean playing = true;

        while (playing && !player.isDead()) {

            System.out.println();

            System.out.println(
                    "You are level " + player.getLevel()
                            + " (" + player.getXp() + "/" + player.getXpToNextLevel() + " XP), "
                            + "with " + player.getCoins() + " coins and "
                            + player.getHealth() + "/" + player.getMaxHealth()
                            + " health."
            );

            System.out.println();

            System.out.println(
                    "What would you like to do?"
            );

            System.out.println(
                    "(Fight, Shop, Heal, Coke, Save, Quit)"
            );

            String choice = input.nextLine();


            // ======================================================
            // FIGHT
            // ======================================================

            if (choice.equalsIgnoreCase("fight")) {

                Area area = chooseArea(input, player.getLevel());

                if (area != null) {
                    Enemy enemy = EnemyFactory.randomEnemy(gen, area);

                    Fight.start(
                            input,
                            gen,
                            player,
                            enemy
                    );
                }
            }


            // ======================================================
            // SHOP
            // ======================================================

            else if (choice.equalsIgnoreCase("shop")) {

                Shop.enter(input, player);
            }


            // ======================================================
            // HEAL
            // ======================================================

            else if (choice.equalsIgnoreCase("heal")) {

                player.usePotion();
            }


            // ======================================================
            // COKE
            // ======================================================

            else if (choice.equalsIgnoreCase("coke")) {

                Items.drinkCoke(gen, player);
            }


            // ======================================================
            // SAVE
            // ======================================================

            else if (choice.equalsIgnoreCase("save")) {

                SaveManager.savePlayer(player);
            }


            // ======================================================
            // QUIT
            // ======================================================

            else if (choice.equalsIgnoreCase("quit")) {

                // Automatically save before quitting
                SaveManager.savePlayer(player);

                playing = false;
            }


            // ======================================================
            // INVALID COMMAND
            // ======================================================

            else {

                System.out.println(
                        "Invalid choice."
                );
            }
        }


        // ==========================================================
        // PLAYER DIED
        // ==========================================================

        if (player.isDead()) {

            // Delete the player's save because they died
            SaveManager.deleteSave(
                    player.getName()
            );

            System.out.println();
            System.out.println("=====================");
            System.out.println("GAME OVER");
            System.out.println("=====================");

            System.out.println(
                    "Your save has been deleted."
            );
        }


        // ==========================================================
        // PLAYER QUIT
        // ==========================================================

        else {

            System.out.println();
            System.out.println(
                    "Thanks for playing!"
            );
        }


        // Close Scanner
        input.close();
    }


    // ==========================================================
    // CHOOSE AREA
    // ==========================================================
    // Lets the player pick which zone to fight in. Shows the
    // level range for each area and flags the recommended one
    // based on the player's current level, but doesn't block
    // picking a harder (or easier) area — that's the player's
    // call. Returns null if the player backs out.
    // ==========================================================

    private static Area chooseArea(Scanner input, int playerLevel) {

        Area recommended = Area.recommendedFor(playerLevel);
        Area[] areas = Area.values();

        System.out.println();
        System.out.println("Choose an area to fight in:");

        for (int i = 0; i < areas.length; i++) {
            Area area = areas[i];
            String tag = (area == recommended) ? "  <- recommended" : "";
            System.out.println(
                    (i + 1) + ". " + area.getDisplayName()
                            + " (Lv " + area.getMinLevel() + "-" + area.getMaxLevel() + ")"
                            + tag
            );
        }

        System.out.println((areas.length + 1) + ". Cancel");

        String choice = input.nextLine();

        try {
            int index = Integer.parseInt(choice.trim());

            if (index >= 1 && index <= areas.length) {
                return areas[index - 1];
            }
        } catch (NumberFormatException e) {
            // fall through to invalid choice handling below
        }

        System.out.println("Cancelled.");
        return null;
    }


    // ==========================================================
    // CREATE PLAYER
    // ==========================================================
    // Takes the class name the player typed and creates the
    // correct type of Player.
    //
    // Example:
    //
    // "Warrior" -> new Warrior(name)
    // "Mage"    -> new Mage(name)
    // "Cleric"  -> new Cleric(name)
    // "Rogue"   -> new Rogue(name)
    // ==========================================================

    private static Player createPlayer(
            String classChoice,
            String name) {

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