import java.util.Random;

// ==========================================================
// ENEMY FACTORY
// ==========================================================
// One place to define every enemy type as data, not code.
// Adding a new mob = adding one line here. No new class,
// no copy-pasted fight loop — that's the whole payoff of
// converting this game to OOP.
// ==========================================================

public class EnemyFactory {

    public static Enemy randomEnemy(Random gen) {
        int roll = gen.nextInt(6) + 1;

        switch (roll) {
            case 1: return new Enemy("Goblin", 8, 4, 3, 1, 3);
            case 2: return new Enemy("Wolf", 15, 6, 2, 2, 5);
            case 3: return new Enemy("Skeleton", 12, 5, 4, 2, 4);
            case 4: return new Enemy("Bandit", 18, 7, 3, 3, 7);
            case 5: return new Enemy("Orc", 25, 9, 5, 4, 9);
            case 6: return new Enemy("Victoria", 40, 12, 8, 10, 19);
            default: return new Enemy("Goblin", 8, 4, 3, 1, 3);
        }
    }
}