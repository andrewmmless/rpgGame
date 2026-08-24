import java.util.*;

public class Main {

    public static void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int heal10(int playerHealth) {
        playerHealth += 10;

        if (playerHealth > 25) {
            playerHealth = 25;
        }
        return playerHealth;
    }

    public static int coins(Random gen, int min, int max) {
        return gen.nextInt(max - min + 1) + min;
    }

    public static int stabAttack(Random gen, int goblinHealth) {
        int hitChance =
                gen.nextInt(100) + 1;

        if (hitChance <= 85) {

            int playerDamage =
                    gen.nextInt(4) + 2;

            goblinHealth -= playerDamage;

            System.out.println("You stabbed the goblin for " + playerDamage + " damage. Goblin health: " + goblinHealth);

        } else {

            System.out.println(
                    "You stabbed and missed!"
            );
        }
        return goblinHealth;
    }


    public static void main(String[] args) {

        System.out.println("Hello welcome to Andrew's adventure game v1");

        Scanner input = new Scanner(System.in);
        Random gen = new Random();

        boolean dead = false;

        // =========================
        // CLASS SELECTION
        // =========================

        System.out.println("In this game we have many classes to choose from!");
        System.out.println(" ");
        System.out.println("Our current working class is Warrior.");
        System.out.println("Your following character class selection is as follows:");
        System.out.println("Warrior");
        System.out.println("Cleric");
        System.out.println("Mage");
        System.out.println("Rogue");
        System.out.println("(Write your class of choice)");

        String className = input.nextLine();

        // =========================
        // WARRIOR SELECTED
        // =========================

        if (className.equalsIgnoreCase("Warrior")) {

            System.out.println("You have selected warrior!");
            pause(500);

            System.out.println("Your base stats are as the following.");
            pause(300);

            System.out.println("Attack: 10, Defense: 5, Mana: 0: Health: 25");
            pause(500);

            System.out.println("Good luck on your adventure Warrior!");
            pause(500);

            // =========================
            // PLAYER STATS
            // =========================

            int playerCoins = 0;
            int playerHealth = 25;
            int playerAttack = 10;
            int playerDefence = 5;
            int playerPotions = 0;

            // =========================
            // MAIN GAME LOOP
            // =========================

            while (!dead) {

                System.out.println("What would you like to do now?");

                boolean alive = false;

                while (!alive && !dead) {

                    System.out.println(
                            "Current options are 'Fight', 'Coke', 'shop', or 'heal'"
                    );

                    String choice1 = input.nextLine();

                    // =========================
                    // SHOP
                    // =========================

                    if (choice1.equalsIgnoreCase("shop")) {

                        System.out.println("You have entered the shop!");
                        pause(500);

                        System.out.println(
                                "You can leave the shop at any time by saying 'Leave'"
                        );

                        pause(500);

                        System.out.println("Current shop options are:");
                        System.out.println("Health Potion");

                        pause(500);

                        System.out.println("Would you like to purchase anything?");

                        String choiceShop = input.nextLine();

                        if (choiceShop.equalsIgnoreCase("Health Potion")
                                && playerCoins >= 10) {

                            playerCoins -= 10;
                            playerPotions += 1;

                            System.out.println(
                                    "You have acquired 1 health potion for 10 coins!"
                            );

                            System.out.println(
                                    "You now have " + playerPotions + " health potion(s)."
                            );

                            System.out.println(
                                    "You now have " + playerCoins + " coins."
                            );

                        } else if (choiceShop.equalsIgnoreCase("Health Potion")
                                && playerCoins < 10) {

                            System.out.println(
                                    "Sorry you do not have 10 coins!"
                            );

                            System.out.println(
                                    "You currently have "
                                            + playerCoins
                                            + " coins."
                            );

                        } else if (choiceShop.equalsIgnoreCase("Leave")) {

                            System.out.println("You left the shop.");

                        } else {

                            System.out.println(
                                    "That is not a valid shop option."
                            );
                        }

                    }

                    // =========================
                    // HEAL
                    // =========================

                    else if (choice1.equalsIgnoreCase("heal")) {
                        if (playerPotions == 0) {

                            System.out.println("Sorry you have no potions.");
                            System.out.println(
                                    "Please purchase 1 from the shop."
                            );

                        } else {
                            playerPotions -= 1;
                            playerHealth = heal10(playerHealth);

                            System.out.println("You used 1 potion and healed for 10 health.");
                            System.out.println("You have " + playerPotions + " potion(s) remaining.");
                        }
                    }

                    System.out.println(
                            "Current health: " + playerHealth
                    );


                    // =========================
                    // COKE
                    // =========================

                    if (choice1.equalsIgnoreCase("Coke")) {

                        pause(800);

                        int cokeRandom = gen.nextInt(2) + 1;

                        if (cokeRandom == 1) {

                            System.out.println(
                                    "Ouuuu yikes. You took cocaine instead of coke."
                            );

                            System.out.println("Player lost 100 health.");

                            dead = true;

                        } else {

                            System.out.println(
                                    "You drank a refreshing coke and gained all your health back!"
                            );

                            playerHealth = 25;
                        }
                    }

                    // =========================
                    // FIGHT
                    // =========================

                    else if (choice1.equalsIgnoreCase("Fight")) {

                        int mob = gen.nextInt(3) + 1;

                        // =====================================================
                        // GOBLIN
                        // =====================================================

                        if (mob == 1) {

                            int goblinHealth = 8;

                            System.out.println("You encountered a Goblin!");
                            pause(500);

                            System.out.println(
                                    "Goblin's health stats are as the following."
                            );

                            System.out.println(
                                    "Attack: 4, Defense: 3, Mana: 0: Health: 8"
                            );

                            pause(500);

                            while (playerHealth > 0 && goblinHealth > 0) {

                                System.out.println(
                                        "Choose 'stab', 'attack', 'KO Slash', or 'run'."
                                );

                                String choiceWarriorEncounter =
                                        input.nextLine();

                                // =========================
                                // RUN
                                // =========================

                                if (choiceWarriorEncounter.equalsIgnoreCase("run")) {

                                    pause(600);

                                    int runChance =
                                            gen.nextInt(100) + 1;

                                    if (runChance <= 30) {

                                        System.out.println(
                                                "You successfully ran away!"
                                        );

                                        break;

                                    } else {

                                        System.out.println(
                                                "You did not successfully run away."
                                        );

                                        pause(500);

                                        int goblinDamage =
                                                gen.nextInt(4) + 1;

                                        playerHealth -= goblinDamage;

                                        System.out.println(
                                                "The goblin attacked and dealt "
                                                        + goblinDamage
                                                        + ". You currently have "
                                                        + playerHealth
                                                        + " health remaining."
                                        );
                                    }
                                }

                                // =========================
                                // STAB
                                // =========================

                                else if (choiceWarriorEncounter.equalsIgnoreCase("stab")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 85) {

                                        int playerDamage =
                                                gen.nextInt(4) + 2;

                                        goblinHealth -= playerDamage;

                                        System.out.println(
                                                "You stabbed the goblin for "
                                                        + playerDamage
                                                        + " damage. Goblin health: "
                                                        + goblinHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You stabbed and missed!"
                                        );
                                    }

                                    if (goblinHealth > 0) {

                                        pause(600);

                                        int goblinDamage =
                                                gen.nextInt(4) + 1;

                                        playerHealth -= goblinDamage;

                                        System.out.println(
                                                "The goblin attacked you for "
                                                        + goblinDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // =========================
                                // ATTACK
                                // =========================

                                else if (choiceWarriorEncounter.equalsIgnoreCase("attack")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 70) {

                                        int playerDamage =
                                                gen.nextInt(4) + 4;

                                        goblinHealth -= playerDamage;

                                        System.out.println(
                                                "You attacked the goblin for "
                                                        + playerDamage
                                                        + " damage. Goblin health: "
                                                        + goblinHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You attacked and missed!"
                                        );
                                    }

                                    if (goblinHealth > 0) {

                                        pause(600);

                                        int goblinDamage =
                                                gen.nextInt(4) + 1;

                                        playerHealth -= goblinDamage;

                                        System.out.println(
                                                "The goblin attacked you for "
                                                        + goblinDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // =========================
                                // KO SLASH
                                // =========================

                                else if (choiceWarriorEncounter.equalsIgnoreCase("KO Slash")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 55) {

                                        int playerDamage =
                                                gen.nextInt(5) + 6;

                                        goblinHealth -= playerDamage;

                                        System.out.println(
                                                "You landed a KO Slash for "
                                                        + playerDamage
                                                        + " damage! Goblin health: "
                                                        + goblinHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "Your KO Slash missed!"
                                        );
                                    }

                                    if (goblinHealth > 0) {

                                        pause(600);

                                        int goblinDamage =
                                                gen.nextInt(4) + 1;

                                        playerHealth -= goblinDamage;

                                        System.out.println(
                                                "The goblin attacked you for "
                                                        + goblinDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // =========================
                                // INVALID CHOICE
                                // =========================

                                else {

                                    System.out.println(
                                            "Invalid choice. Please choose stab, attack, KO Slash, or run."
                                    );

                                    continue;
                                }

                                // =========================
                                // GOBLIN DEFEATED
                                // =========================

                                if (goblinHealth <= 0) {

                                    pause(700);

                                    System.out.println("Killed goblin!");

                                    int coinsGained = coins(gen, 2, 5);

                                    playerCoins += coinsGained;

                                    System.out.println(
                                            "You have gained "
                                                    + coinsGained
                                                    + " coins. Your new total is "
                                                    + playerCoins
                                    );

                                    break;
                                }

                                // =========================
                                // PLAYER DEATH
                                // =========================

                                if (playerHealth <= 0) {

                                    pause(800);

                                    System.out.println(
                                            "Sorry you died try again next game"
                                    );

                                    dead = true;

                                    break;
                                }
                            }
                        }

                        // =====================================================
                        // WOLF
                        // =====================================================

                        else if (mob == 2) {

                            int wolfHealth = 15;

                            System.out.println("You encountered a Wolf!");
                            pause(500);

                            System.out.println(
                                    "Wolf's health stats are as the following."
                            );

                            System.out.println(
                                    "Attack: 6, Defense: 2, Mana: 0: Health: 15"
                            );

                            pause(500);

                            while (playerHealth > 0 && wolfHealth > 0) {

                                System.out.println(
                                        "Choose 'stab', 'attack', 'KO Slash', or 'run'."
                                );

                                String choiceWarriorEncounter =
                                        input.nextLine();

                                // RUN
                                if (choiceWarriorEncounter.equalsIgnoreCase("run")) {

                                    pause(600);

                                    int runChance =
                                            gen.nextInt(100) + 1;

                                    if (runChance <= 30) {

                                        System.out.println(
                                                "You successfully ran away!"
                                        );

                                        break;

                                    } else {

                                        System.out.println(
                                                "You did not successfully run away."
                                        );

                                        pause(500);

                                        int wolfDamage =
                                                gen.nextInt(6) + 2;

                                        playerHealth -= wolfDamage;

                                        System.out.println(
                                                "The wolf attacked and dealt "
                                                        + wolfDamage
                                                        + ". You currently have "
                                                        + playerHealth
                                                        + " health remaining."
                                        );
                                    }
                                }

                                // STAB
                                else if (choiceWarriorEncounter.equalsIgnoreCase("stab")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 85) {

                                        int playerDamage =
                                                gen.nextInt(5) + 2;

                                        wolfHealth -= playerDamage;

                                        System.out.println(
                                                "You stabbed the wolf for "
                                                        + playerDamage
                                                        + " damage. Wolf health: "
                                                        + wolfHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You stabbed and missed!"
                                        );
                                    }

                                    if (wolfHealth > 0) {

                                        pause(600);

                                        int wolfDamage =
                                                gen.nextInt(6) + 2;

                                        playerHealth -= wolfDamage;

                                        System.out.println(
                                                "The wolf attacked you for "
                                                        + wolfDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // ATTACK
                                else if (choiceWarriorEncounter.equalsIgnoreCase("attack")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 70) {

                                        int playerDamage =
                                                gen.nextInt(6) + 4;

                                        wolfHealth -= playerDamage;

                                        System.out.println(
                                                "You attacked the wolf for "
                                                        + playerDamage
                                                        + " damage. Wolf health: "
                                                        + wolfHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You attacked and missed!"
                                        );
                                    }

                                    if (wolfHealth > 0) {

                                        pause(600);

                                        int wolfDamage =
                                                gen.nextInt(6) + 2;

                                        playerHealth -= wolfDamage;

                                        System.out.println(
                                                "The wolf attacked you for "
                                                        + wolfDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // KO SLASH
                                else if (choiceWarriorEncounter.equalsIgnoreCase("KO Slash")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 55) {

                                        int playerDamage =
                                                gen.nextInt(9) + 8;

                                        wolfHealth -= playerDamage;

                                        System.out.println(
                                                "You landed a KO Slash on the wolf for "
                                                        + playerDamage
                                                        + " damage! Wolf health: "
                                                        + wolfHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "Your KO Slash missed!"
                                        );
                                    }

                                    if (wolfHealth > 0) {

                                        pause(600);

                                        int wolfDamage =
                                                gen.nextInt(6) + 2;

                                        playerHealth -= wolfDamage;

                                        System.out.println(
                                                "The wolf attacked you for "
                                                        + wolfDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // INVALID
                                else {

                                    System.out.println(
                                            "Invalid choice. Please choose stab, attack, KO Slash, or run."
                                    );

                                    continue;
                                }

                                // WOLF DEFEATED
                                if (wolfHealth <= 0) {

                                    pause(700);

                                    System.out.println("Killed wolf!");

                                    int coinsGained =
                                            gen.nextInt(4) + 2;

                                    playerCoins += coinsGained;

                                    System.out.println(
                                            "You have gained "
                                                    + coinsGained
                                                    + " coins. Your new total is "
                                                    + playerCoins
                                    );

                                    break;
                                }

                                // PLAYER DEATH
                                if (playerHealth <= 0) {

                                    pause(800);

                                    System.out.println(
                                            "Sorry you died try again next game"
                                    );

                                    dead = true;

                                    break;
                                }
                            }
                        }

                        // =====================================================
                        // VICTORIA
                        // =====================================================

                        else if (mob == 3) {

                            int victoriaHealth = 40;

                            System.out.println(
                                    "You encountered a Victoria!"
                            );

                            pause(500);

                            System.out.println(
                                    "Victoria's health stats are as the following."
                            );

                            System.out.println(
                                    "Attack: 12, Defense: 8, Mana: 0: Health: 40"
                            );

                            pause(500);

                            while (playerHealth > 0 && victoriaHealth > 0) {

                                System.out.println(
                                        "Choose 'stab', 'attack', 'KO Slash', or 'run'."
                                );

                                String choiceWarriorEncounter =
                                        input.nextLine();

                                // RUN
                                if (choiceWarriorEncounter.equalsIgnoreCase("run")) {

                                    pause(600);

                                    int runChance =
                                            gen.nextInt(100) + 1;

                                    if (runChance <= 30) {

                                        System.out.println(
                                                "You successfully ran away!"
                                        );

                                        break;

                                    } else {

                                        System.out.println(
                                                "You did not successfully run away."
                                        );

                                        pause(500);

                                        int victoriaDamage =
                                                gen.nextInt(10) + 5;

                                        playerHealth -= victoriaDamage;

                                        System.out.println(
                                                "Victoria attacked and dealt "
                                                        + victoriaDamage
                                                        + ". You currently have "
                                                        + playerHealth
                                                        + " health remaining."
                                        );
                                    }
                                }

                                // STAB
                                else if (choiceWarriorEncounter.equalsIgnoreCase("stab")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 85) {

                                        int playerDamage =
                                                gen.nextInt(5) + 2;

                                        victoriaHealth -= playerDamage;

                                        System.out.println(
                                                "You stabbed Victoria for "
                                                        + playerDamage
                                                        + " damage. Victoria health: "
                                                        + victoriaHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You stabbed and missed!"
                                        );
                                    }

                                    if (victoriaHealth > 0) {

                                        pause(600);

                                        int victoriaDamage =
                                                gen.nextInt(10) + 5;

                                        playerHealth -= victoriaDamage;

                                        System.out.println(
                                                "Victoria attacked you for "
                                                        + victoriaDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // ATTACK
                                else if (choiceWarriorEncounter.equalsIgnoreCase("attack")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 70) {

                                        int playerDamage =
                                                gen.nextInt(6) + 4;

                                        victoriaHealth -= playerDamage;

                                        System.out.println(
                                                "You attacked Victoria for "
                                                        + playerDamage
                                                        + " damage. Victoria health: "
                                                        + victoriaHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "You attacked and missed!"
                                        );
                                    }

                                    if (victoriaHealth > 0) {

                                        pause(600);

                                        int victoriaDamage =
                                                gen.nextInt(10) + 5;

                                        playerHealth -= victoriaDamage;

                                        System.out.println(
                                                "Victoria attacked you for "
                                                        + victoriaDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // KO SLASH
                                else if (choiceWarriorEncounter.equalsIgnoreCase("KO Slash")) {

                                    pause(400);

                                    int hitChance =
                                            gen.nextInt(100) + 1;

                                    if (hitChance <= 55) {

                                        int playerDamage =
                                                gen.nextInt(9) + 8;

                                        victoriaHealth -= playerDamage;

                                        System.out.println(
                                                "You landed a KO Slash on Victoria for "
                                                        + playerDamage
                                                        + " damage! Victoria health: "
                                                        + victoriaHealth
                                        );

                                    } else {

                                        System.out.println(
                                                "Your KO Slash missed!"
                                        );
                                    }

                                    if (victoriaHealth > 0) {

                                        pause(600);

                                        int victoriaDamage =
                                                gen.nextInt(10) + 5;

                                        playerHealth -= victoriaDamage;

                                        System.out.println(
                                                "Victoria attacked you for "
                                                        + victoriaDamage
                                                        + " damage. Your health: "
                                                        + playerHealth
                                        );
                                    }
                                }

                                // INVALID
                                else {

                                    System.out.println(
                                            "Invalid choice. Please choose stab, attack, KO Slash, or run."
                                    );

                                    continue;
                                }

                                // VICTORIA DEFEATED
                                if (victoriaHealth <= 0) {

                                    pause(700);

                                    System.out.println(
                                            "Defeated Victoria!"
                                    );

                                    int coinsGained =
                                            gen.nextInt(10) + 10;

                                    playerCoins += coinsGained;

                                    System.out.println(
                                            "You have gained "
                                                    + coinsGained
                                                    + " coins. Your new total is "
                                                    + playerCoins
                                    );

                                    break;
                                }

                                // PLAYER DEATH
                                if (playerHealth <= 0) {

                                    pause(800);

                                    System.out.println(
                                            "Sorry you died try again next game"
                                    );

                                    dead = true;

                                    break;
                                }
                            }
                        }
                    }

                    // =========================
                    // INVALID MAIN MENU CHOICE
                    // =========================

                    else {

                        System.out.println(
                                "Invalid choice. Please choose Fight, Coke, shop, or heal."
                        );
                    }

                    pause(1000);
                }
            }

            // =========================
            // GAME OVER
            // =========================

            if (dead) {

                pause(1000);

                System.out.println("=====================");
                System.out.println("GAME OVER");
                System.out.println("=====================");
            }
        }


    }
}


