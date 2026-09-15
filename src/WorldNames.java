import java.util.*;

/** Canonical working names from the Wayfarer design plan. */
public final class WorldNames {
    private WorldNames() {}
    public static final String KINGDOM="Cindergard";
    public static List<String> ranks(PlayerClass type) {
        return switch(type) {
            case WARRIOR -> List.of("Squire","Knight","Knight-Lieutenant","Knight-Captain","Knight Commander");
            case MAGE -> List.of("Apprentice","Battle Mage","Court Mage","Royal Magus","Archmagus");
            case CLERIC -> List.of("Acolyte","Field Healer","Court Healer","High Healer","Grand Healer");
            case ROGUE -> List.of("Scout","Ranger","Royal Scout","Spymaster","Shadow Warden");
        };
    }
    public static String rank(PlayerClass type,Set<String> cleared) {
        int milestones=0;for(Area area:Area.values()){if(!cleared.contains(area.name()))break;milestones++;}
        return ranks(type).get(milestones);
    }
    public static List<String> subAreas(Area area) {
        return switch(area) {
            case WHISPERING_WOODS -> List.of("Old Timberline","Hollow Reach","Blighted Grove");
            case STONEFANG_CAVES -> List.of("Entrance Tunnels","Deep Shaft","The Fracture");
            case FORGOTTEN_RUINS -> List.of("Outer Colonnade","Sunken Hall","The Drowned Archive");
            case DRAGONS_SPIRE -> List.of("Ashen Approach","The Scarred Path","The Wyrm's Hollow");
        };
    }
    public static String gatheringArea(Area area) {
        return switch(area) {
            case WHISPERING_WOODS -> "the Blighted Hollow";
            case STONEFANG_CAVES -> "the Hollow Vein";
            case FORGOTTEN_RUINS -> "the Drowned Archive";
            case DRAGONS_SPIRE -> "";
        };
    }
}
