import java.util.*;

/** Versioned persisted state. No polymorphic class names or client-supplied Java types. */
public record GameSave(int schemaVersion, PlayerData player, String mode, Area area, int room,
                       int kills, int deaths, int towerBest, int towerFloor,
                       Set<String> cleared, Set<String> claimed, List<Equipment> inventory,
                       Map<Equipment.Slot,String> equipped, List<String> log, BattleData battle) {
    public record PlayerData(String name, PlayerClass type, int level, int xp, int health,
                             int resource, int coins, int potions, int swordDamage) {}
    public record BattleData(String name, int level, int maxHealth, int health, int attack,
                             int defence, boolean boss, int round, Map<String,Integer> cooldowns,
                             List<Character.StatusState> playerStatuses, List<Character.StatusState> enemyStatuses) {}
}
