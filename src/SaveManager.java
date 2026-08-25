import java.io.*;

public class SaveManager {

    private static final String SAVE_FOLDER = "saves";

    // ==========================================================
    // SAVE PLAYER
    // ==========================================================

    public static void savePlayer(Player player) {

        File folder = new File(SAVE_FOLDER);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = SAVE_FOLDER + "/" + player.getName() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            writer.println("Name=" + player.getName());
            writer.println("Class=" + getPlayerClass(player));
            writer.println("Health=" + player.getHealth());
            writer.println("MaxHealth=" + player.getMaxHealth());
            writer.println("AttackPower=" + player.getAttackPower());
            writer.println("Defence=" + player.getDefence());
            writer.println("Coins=" + player.getCoins());
            writer.println("Potions=" + player.getPotions());
            writer.println("SwordDamage=" + player.getSwordDamage());
            writer.println("Level=" + player.getLevel());
            writer.println("Xp=" + player.getXp());
            writer.println("XpToNextLevel=" + player.getXpToNextLevel());

            System.out.println("Game saved successfully!");

        } catch (IOException e) {

            System.out.println("Could not save the game.");
            e.printStackTrace();
        }
    }


    // ==========================================================
    // LOAD PLAYER
    // ==========================================================

    public static Player loadPlayer(String playerName) {

        String fileName = SAVE_FOLDER + "/" + playerName + ".txt";
        File file = new File(fileName);

        if (!file.exists()) {

            System.out.println(
                    "No save file found for " + playerName + "."
            );

            return null;
        }

        String name = "";
        String className = "";

        int health = 0;
        int maxHealth = 0;
        int attackPower = 0;
        int defence = 0;
        int coins = 0;
        int potions = 0;
        int swordDamage = 0;
        int level = 1;
        int xp = 0;
        int xpToNextLevel = 20;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0];
                String value = parts[1];

                switch (key) {

                    case "Name":
                        name = value;
                        break;

                    case "Class":
                        className = value;
                        break;

                    case "Health":
                        health = Integer.parseInt(value);
                        break;

                    case "MaxHealth":
                        maxHealth = Integer.parseInt(value);
                        break;

                    case "AttackPower":
                        attackPower = Integer.parseInt(value);
                        break;

                    case "Defence":
                        defence = Integer.parseInt(value);
                        break;

                    case "Coins":
                        coins = Integer.parseInt(value);
                        break;

                    case "Potions":
                        potions = Integer.parseInt(value);
                        break;

                    case "SwordDamage":
                        swordDamage = Integer.parseInt(value);
                        break;

                    case "Level":
                        level = Integer.parseInt(value);
                        break;

                    case "Xp":
                        xp = Integer.parseInt(value);
                        break;

                    case "XpToNextLevel":
                        xpToNextLevel = Integer.parseInt(value);
                        break;
                }
            }


            // ======================================================
            // CREATE THE CORRECT PLAYER CLASS
            // ======================================================

            Player player;

            if (className.equalsIgnoreCase("Warrior")) {

                player = new Warrior(name);

            } else if (className.equalsIgnoreCase("Mage")) {

                player = new Mage(name);

            } else if (className.equalsIgnoreCase("Cleric")) {

                player = new Cleric(name);

            } else if (className.equalsIgnoreCase("Rogue")) {

                player = new Rogue(name);

            } else {

                System.out.println(
                        "Unknown player class in save file."
                );

                return null;
            }


            // ======================================================
            // RESTORE SAVED STATS
            // ======================================================

            player.setHealth(health);
            player.setMaxHealth(maxHealth);
            player.setAttackPower(attackPower);
            player.setDefence(defence);
            player.setCoins(coins);
            player.setPotions(potions);
            player.setSwordDamage(swordDamage);
            player.setLevel(level);
            player.setXp(xp);
            player.setXpToNextLevel(xpToNextLevel);

            System.out.println("Game loaded successfully!");

            return player;

        } catch (IOException | NumberFormatException e) {

            System.out.println("Could not load the game.");
            e.printStackTrace();

            return null;
        }
    }


    // ==========================================================
    // GET PLAYER CLASS
    // ==========================================================

    private static String getPlayerClass(Player player) {

        if (player instanceof Warrior) {

            return "Warrior";

        } else if (player instanceof Mage) {

            return "Mage";

        } else if (player instanceof Cleric) {

            return "Cleric";

        } else if (player instanceof Rogue) {

            return "Rogue";
        }

        return "Unknown";
    }


    // ==========================================================
    // DELETE SAVE
    // ==========================================================

    public static void deleteSave(String playerName) {

        String fileName = SAVE_FOLDER + "/" + playerName + ".txt";

        File file = new File(fileName);

        if (!file.exists()) {

            System.out.println(
                    "No save file found for " + playerName + "."
            );

            return;
        }

        if (file.delete()) {

            System.out.println(
                    "Save for " + playerName
                            + " deleted successfully!"
            );

        } else {

            System.out.println(
                    "Could not delete the save."
            );
        }
    }
}