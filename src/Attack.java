import java.util.*;
public class Attack {
    // =========================
    // STAB ATTACK METHOD
    // =========================

    int stabAttack(Random gen, int enemyHealth, String enemyName, int swordDamage) {

        int hitChance = gen.nextInt(100) + 1;

        if (hitChance <= 85) {

            int playerDamage = gen.nextInt(4) + 2 + swordDamage;

            enemyHealth -= playerDamage;

            System.out.println(
                    "You stabbed the " +
                            enemyName +
                            " for " +
                            playerDamage +
                            " damage. " +
                            enemyName +
                            " health: " +
                            enemyHealth
            );

        } else {

            System.out.println("You stabbed and missed!");
        }

        return enemyHealth;
    }
    int killAttack(Random gen, int enemyHealth, String enemyName, int swordDamage) {


        int playerDamage = 1000;

        enemyHealth -= playerDamage;

        System.out.println(
                "You BLASTED the " +
                        enemyName +
                        " for " +
                        playerDamage +
                        " damage. " +
                        enemyName +
                        " health: " +
                        enemyHealth
        );
        return enemyHealth;
    }

    // =========================
    // NORMAL ATTACK METHOD
    // =========================

int normalAttack(
            Random gen,
            int enemyHealth,
            String enemyName,
            int damageMax,
            int swordDamage) {

        int hitChance = gen.nextInt(100) + 1;

        if (hitChance <= 70) {

            int playerDamage =
                    gen.nextInt(damageMax - 4 + 1)
                            + 4
                            + swordDamage;

            enemyHealth -= playerDamage;

            System.out.println(
                    "You attacked the " +
                            enemyName +
                            " for " +
                            playerDamage +
                            " damage. " +
                            enemyName +
                            " health: " +
                            enemyHealth
            );

        } else {

            System.out.println("You attacked and missed!");
        }

        return enemyHealth;
    }

    // =========================
    // KO SLASH METHOD
    // =========================

    public static int koSlash(
            Random gen,
            int enemyHealth,
            String enemyName,
            int damageMax,
            int swordDamage) {

        int hitChance = gen.nextInt(100) + 1;

        if (hitChance <= 55) {

            int playerDamage =
                    gen.nextInt(damageMax - 8 + 1)
                            + 8
                            + swordDamage;

            enemyHealth -= playerDamage;

            System.out.println(
                    "You landed a KO Slash on the " +
                            enemyName +
                            " for " +
                            playerDamage +
                            " damage! " +
                            enemyName +
                            " health: " +
                            enemyHealth
            );

        } else {

            System.out.println("Your KO Slash missed!");
        }

        return enemyHealth;
    }

}
