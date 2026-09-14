import java.util.*;

/** Original areas and enemies, with shared V4 stat and reward curves. */
public final class EnemyFactory {
    private record Definition(String name,int level,int coinMin,int coinMax,boolean boss) {
        Enemy create() {
            int health=(int)((28+6*(level-1))*(boss?1.6:1));
            int attack=(int)((9+2*(level-1))*(boss?1.2:1));
            int xp=Balance.enemyXp(level)*(boss?2:1);
            return new Enemy(name,level,health,attack,4+level,coinMin,coinMax,xp,xp);
        }
    }
    private static final Map<Area,List<Definition>> ENEMIES=Map.of(
        Area.WHISPERING_WOODS,List.of(
            new Definition("Rat",1,1,2,false),
            new Definition("Goblin",1,2,4,false),
            new Definition("Wild Boar",2,3,5,false),
            new Definition("Bandit Scout",2,3,6,false),
            new Definition("Forest Wolf",3,4,7,false),
            new Definition("Alpha Wolf",4,10,15,true)
        ),
        Area.STONEFANG_CAVES,List.of(
            new Definition("Cave Spider",5,5,8,false),
            new Definition("Skeleton",6,6,10,false),
            new Definition("Bandit",6,7,12,false),
            new Definition("Orc Grunt",7,9,14,false),
            new Definition("Cave Troll",8,11,17,false),
            new Definition("Orc Chieftain",9,25,35,true)
        ),
        Area.FORGOTTEN_RUINS,List.of(
            new Definition("Skeleton Knight",10,15,22,false),
            new Definition("Wraith",11,16,24,false),
            new Definition("Stone Golem",12,18,26,false),
            new Definition("Dark Cultist",13,20,28,false),
            new Definition("Gargoyle",14,22,30,false),
            new Definition("Lich Acolyte",15,45,60,true)
        ),
        Area.DRAGONS_SPIRE,List.of(
            new Definition("Wyvern",16,35,45,false),
            new Definition("Frost Elemental",17,38,48,false),
            new Definition("Shadow Knight",18,42,55,false),
            new Definition("Storm Harpy",19,40,50,false),
            new Definition("Ancient Guardian",21,50,65,false),
            new Definition("Victoria the Dragon",25,150,220,true)
        )
    );
    public static Enemy randomEnemy(Random random,Area area,int playerLevel) {
        Objects.requireNonNull(random); Objects.requireNonNull(area);
        if(playerLevel<1 || playerLevel>Balance.MAX_LEVEL)throw new IllegalArgumentException("Invalid level");
        int unlocked=Math.min(6,3+Math.max(0,playerLevel-area.getMinLevel()));
        return ENEMIES.get(area).get(random.nextInt(unlocked)).create();
    }
}
