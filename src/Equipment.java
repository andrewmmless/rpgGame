import java.util.*;

public record Equipment(String id, String name, Slot slot, Rarity rarity, int level, int power, int upgrades, WeaponAttribute attribute) {
    public enum Slot { WEAPON, ARMOUR }
    public enum Rarity { COMMON, RARE, EPIC }
    public Equipment(String id,String name,Slot slot,Rarity rarity,int level,int power,int upgrades) {
        this(id,name,slot,rarity,level,power,upgrades,WeaponAttribute.NONE);
    }
    public Equipment {
        Objects.requireNonNull(id); Objects.requireNonNull(name); Objects.requireNonNull(slot); Objects.requireNonNull(rarity);
        if(attribute==null)attribute=WeaponAttribute.NONE; // Existing saves predate weapon attributes.
        if(slot==Slot.ARMOUR && attribute!=WeaponAttribute.NONE)throw new IllegalArgumentException("Weapon attributes require a weapon");
        if (level < 1 || power < 1 || upgrades < 0 || upgrades > 5) throw new IllegalArgumentException("Invalid item");
    }
    public int value() { return 2 + level + rarity.ordinal() * 4 + upgrades * 2; }
    public int upgradeCost() { return 15 + level * 3 + upgrades * 15; }
    public Equipment upgrade() {
        if(upgrades == 5) throw new IllegalArgumentException("This item is fully upgraded.");
        return new Equipment(id,name,slot,rarity,level,power+2,upgrades+1,attribute);
    }
    public static String weaponName(PlayerClass type,Rarity rarity) {
        String[] names=switch(type){
            case WARRIOR -> new String[]{"Iron Longsword","Runebound Edge","Dawnbringer"};
            case MAGE -> new String[]{"Ashwood Staff","Runebound Staff","Starfire Staff"};
            case CLERIC -> new String[]{"Iron Mace","Sanctified Mace","Dawnkeeper's Sceptre"};
            case ROGUE -> new String[]{"Iron Daggers","Nightsteel Daggers","Whisperfangs"};
        };return names[rarity.ordinal()];
    }
    public static Equipment drop(Random random, int level, boolean boss) {
        return drop(random,level,boss,PlayerClass.WARRIOR);
    }
    public static Equipment drop(Random random,int level,boolean boss,PlayerClass type) {
        Slot slot = random.nextBoolean() ? Slot.WEAPON : Slot.ARMOUR;
        int roll = random.nextInt(100);
        Rarity rarity = boss ? (roll < 30 ? Rarity.EPIC : Rarity.RARE) : roll < 8 ? Rarity.EPIC : roll < 35 ? Rarity.RARE : Rarity.COMMON;
        String[] armour = {"Traveler's Coat", "Warden's Mail", "Dragonscale Mantle"};
        String name = slot == Slot.WEAPON ? weaponName(type,rarity) : armour[rarity.ordinal()];
        WeaponAttribute attribute=slot==Slot.WEAPON&&rarity!=Rarity.COMMON?WeaponAttribute.values()[1+random.nextInt(3)]:WeaponAttribute.NONE;
        return new Equipment(UUID.randomUUID().toString(),name,slot,rarity,level,2+level/2+rarity.ordinal()*3,0,attribute);
    }
}
