import java.util.Random;

// ==========================================================
// ENEMY FACTORY
// ==========================================================
// One place to define every enemy type as data, not code.
// Adding a new mob = adding one line here. No new class,
// no copy-pasted fight loop — that's the whole payoff of
// converting this game to OOP.
//
// Enemies are now grouped by Area. Each area has 5 regular
// mobs plus one tougher "mini-boss" mob that shows up rarely
// (a 1-in-6 roll) and pays out much better rewards. Stats
// climb steeply from one area to the next so the player
// actually needs to level up before pushing into a new zone.
// ==========================================================

public class EnemyFactory {

    public static Enemy randomEnemy(Random gen, Area area) {
        switch (area) {
            case WHISPERING_WOODS: return whisperingWoods(gen);
            case STONEFANG_CAVES:  return stonefangCaves(gen);
            case FORGOTTEN_RUINS:  return forgottenRuins(gen);
            case DRAGONS_SPIRE:    return dragonsSpire(gen);
            default:               return whisperingWoods(gen);
        }
    }

    // ----------------------------------------------------------
    // AREA 1 — Whispering Woods (levels 1-4)
    // ----------------------------------------------------------
    private static Enemy whisperingWoods(Random gen) {
        int roll = gen.nextInt(6) + 1;
        switch (roll) {
            case 1: return new Enemy("Rat",          1,  6,  3, 1, 1, 2,  4,  6);
            case 2: return new Enemy("Goblin",       1, 10,  4, 2, 2, 4,  6,  9);
            case 3: return new Enemy("Wild Boar",    2, 14,  5, 2, 3, 5,  8, 11);
            case 4: return new Enemy("Bandit Scout", 2, 12,  6, 1, 3, 6,  8, 12);
            case 5: return new Enemy("Forest Wolf",  3, 16,  6, 3, 4, 7, 10, 14);
            case 6: return new Enemy("Alpha Wolf",   4, 28,  8, 4, 10, 15, 25, 30); // mini-boss
            default: return new Enemy("Rat", 1, 6, 3, 1, 1, 2, 4, 6);
        }
    }

    // ----------------------------------------------------------
    // AREA 2 — Stonefang Caves (levels 5-9)
    // ----------------------------------------------------------
    private static Enemy stonefangCaves(Random gen) {
        int roll = gen.nextInt(6) + 1;
        switch (roll) {
            case 1: return new Enemy("Cave Spider",   5, 20,  7,  3,  5,  8, 14, 18);
            case 2: return new Enemy("Skeleton",      6, 26,  8,  5,  6, 10, 16, 20);
            case 3: return new Enemy("Bandit",        6, 30,  9,  4,  7, 12, 18, 24);
            case 4: return new Enemy("Orc Grunt",     7, 38, 11,  6,  9, 14, 22, 28);
            case 5: return new Enemy("Cave Troll",    8, 46, 12,  7, 11, 17, 25, 32);
            case 6: return new Enemy("Orc Chieftain", 9, 65, 15,  9, 25, 35, 55, 65); // mini-boss
            default: return new Enemy("Cave Spider", 5, 20, 7, 3, 5, 8, 14, 18);
        }
    }

    // ----------------------------------------------------------
    // AREA 3 — Forgotten Ruins (levels 10-15)
    // ----------------------------------------------------------
    private static Enemy forgottenRuins(Random gen) {
        int roll = gen.nextInt(6) + 1;
        switch (roll) {
            case 1: return new Enemy("Skeleton Knight", 10,  55, 14,  9, 15, 22, 35, 42);
            case 2: return new Enemy("Wraith",           11,  48, 17,  6, 16, 24, 38, 45);
            case 3: return new Enemy("Stone Golem",      12,  80, 15, 14, 18, 26, 42, 50);
            case 4: return new Enemy("Dark Cultist",     13,  60, 18,  8, 20, 28, 45, 52);
            case 5: return new Enemy("Gargoyle",         14,  70, 16, 12, 22, 30, 48, 56);
            case 6: return new Enemy("Lich Acolyte",     15, 110, 22, 12, 45, 60, 90, 110); // mini-boss
            default: return new Enemy("Skeleton Knight", 10, 55, 14, 9, 15, 22, 35, 42);
        }
    }

    // ----------------------------------------------------------
    // AREA 4 — Dragon's Spire (levels 16-25)
    // ----------------------------------------------------------
    private static Enemy dragonsSpire(Random gen) {
        int roll = gen.nextInt(6) + 1;
        switch (roll) {
            case 1: return new Enemy("Wyvern",           16, 120, 22, 12,  35,  45, 100, 120);
            case 2: return new Enemy("Frost Elemental",  17, 140, 20, 16,  38,  48, 105, 125);
            case 3: return new Enemy("Shadow Knight",    18, 150, 25, 15,  42,  55, 115, 135);
            case 4: return new Enemy("Storm Harpy",      19, 100, 28,  8,  40,  50, 110, 130);
            case 5: return new Enemy("Ancient Guardian", 21, 200, 24, 20,  50,  65, 140, 160);
            case 6: return new Enemy("Victoria the Dragon", 25, 350, 35, 25, 150, 220, 400, 500); // boss
            default: return new Enemy("Wyvern", 16, 120, 22, 12, 35, 45, 100, 120);
        }
    }
}
