import java.util.*;

/** Data for the existing world, with one authored encounter path per region. */
public final class Campaign {
    public record Region(Area area, String subtitle, String story, String boss, String quest, String[] enemies) {}
    public static final List<Region> REGIONS = List.of(
        new Region(Area.WHISPERING_WOODS,"Where the road begins","The old trail has fallen silent. Track the pack to its den and reopen the road to Stonefang.","Alpha Wolf","Silence in the Woods",new String[]{"Rat","Goblin","Wild Boar","Bandit Scout","Forest Wolf"}),
        new Region(Area.STONEFANG_CAVES,"Beneath the mountain","The miners fled when the drums began. Follow the abandoned lamps and confront the Orc Chieftain.","Orc Chieftain","The Drums Below",new String[]{"Cave Spider","Skeleton","Bandit","Orc Grunt","Cave Troll"}),
        new Region(Area.FORGOTTEN_RUINS,"A kingdom remembered","A cold light burns in the ruined chapel. Break the acolyte's ritual before the dead reach town.","Lich Acolyte","The Last Bell",new String[]{"Skeleton Knight","Wraith","Stone Golem","Dark Cultist","Gargoyle"}),
        new Region(Area.DRAGONS_SPIRE,"Beyond the storm","Victoria holds the summit. Climb the broken stair, survive her breath, and bring the long night to an end.","Victoria the Dragon","At the End of the Sky",new String[]{"Wyvern","Frost Elemental","Shadow Knight","Storm Harpy","Ancient Guardian"}));
    public static Region region(Area area) { return REGIONS.get(area.ordinal()); }
    public static Enemy encounter(Area area, int playerLevel, boolean boss, Random random, int towerFloor) {
        Region region=region(area);
        int level = towerFloor>0 ? Math.min(100,playerLevel+Math.min(8,towerFloor/3)) : Math.max(area.getMinLevel(), Math.min(area.getMaxLevel(), playerLevel));
        String name=towerFloor>0 ? (boss?"Tower Sentinel":"Tower Shade") : boss?region.boss():region.enemies()[random.nextInt(region.enemies().length)];
        int hp=(int)((28+6*(level-1))*(boss?1.9:1));
        int attack=(int)((9+2*(level-1))*(boss?1.15:1));
        int xp=Balance.enemyXp(level)*(boss?2:1);
        Enemy enemy=new Enemy(name,level,hp,attack,4+level,4+level,8+level*2,xp,xp);
        return boss?enemy.asBoss():enemy;
    }
}
