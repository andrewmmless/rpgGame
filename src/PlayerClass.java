public enum PlayerClass {
    WARRIOR(100, 13, 12), MAGE(80, 15, 5), CLERIC(95, 12, 9), ROGUE(85, 14, 7);
    final int health, attack, armour;
    PlayerClass(int health, int attack, int armour) { this.health=health; this.attack=attack; this.armour=armour; }
    public Player create(String name) {
        return switch(this) {
            case WARRIOR -> new Warrior(name); case MAGE -> new Mage(name);
            case CLERIC -> new Cleric(name); case ROGUE -> new Rogue(name);
        };
    }
}
