import java.util.*;
/** Optional dungeon contracts; unlocks follow story, not account level. */
public record DungeonContract(String id,String name,String boss,Area area,int minLevel,int maxLevel,int requiredRoute,String description){
 public static final List<DungeonContract> ALL=List.of(
  new DungeonContract("ROOTBOUND","The Rootbound Gate","Rootbound Warden",Area.WHISPERING_WOODS,1,4,-1,"A living ward bars the woodland court. Alternate protection and attacks through its sweeping roots."),
  new DungeonContract("FORGE","The Sealed Forge","Ironbound Chieftain",Area.STONEFANG_CAVES,16,25,2,"A stolen royal forge arms the mountain blockade. Break the chieftain's guard with damage and defend against heavy sweeps."),
  new DungeonContract("ARCHIVE","The Lantern Crypt","Lantern Lich",Area.FORGOTTEN_RUINS,31,40,5,"A keeper of stolen memories feeds on the buried ward. Use a damaging ability to interrupt its recovery; protect your partner from its spells.")
 );
 public static DungeonContract get(String id){return ALL.stream().filter(c->c.id.equals(id==null?"ROOTBOUND":id)).findFirst().orElseThrow(()->new IllegalArgumentException("Choose a dungeon contract."));}
 public boolean unlocked(GameSave save){return requiredRoute<0||SubArea.get(requiredRoute).complete(save.claimed(),save.cleared());}
 public static List<Map<String,Object>> view(GameSave save){return ALL.stream().map(c->Map.<String,Object>of("id",c.id,"name",c.name,"description",c.description,"minLevel",c.minLevel,"maxLevel",c.maxLevel,"unlocked",c.unlocked(save),"requirement",c.requiredRoute<0?"Open to all adventurers":"Complete "+SubArea.get(c.requiredRoute).name())).toList();}
}
