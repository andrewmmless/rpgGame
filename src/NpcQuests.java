import java.util.*;
/** Small, persistent requests offered by named capital residents. */
public final class NpcQuests {
 private NpcQuests(){}
 public record Quest(String id,String place,String speaker,String title,String request,String ending,int route,Gathering material,int amount,int coins){}
 public static final List<Quest> ALL=List.of(
  new Quest("warm_beds","inn","Mara","Beds for the displaced","Families from the eastern road are sleeping on benches. Bring eight Timber so we can build beds.","Mara opens the new room. The families finally have beds, and a place at the breakfast table.",0,Gathering.FORAGING,8,30),
  new Quest("miners_lamps","forge","Iven","Light for the miners","Rowan's rescue crews need lamp bases. Bring ten Stone from the Hollow Vein.","Iven sends the lamps to Rowan. Their light will mark the safe path out of Stonefang.",3,Gathering.MINING,10,45),
  new Quest("relief_meals","market","Nell","Meals for the watch","The families rescued from the ruined quarter need food. Bring eight River fish from the island shallows.","Nell packs the meals for Tessa. The evening watch shares its first warm supper in days.",6,Gathering.FISHING,8,40));
 public static List<Map<String,Object>> view(Set<String> flags,Set<String> cleared){return ALL.stream().filter(q->SubArea.get(q.route()).unlocked(flags,cleared)).map(q->Map.<String,Object>of("id",q.id(),"place",q.place(),"speaker",q.speaker(),"title",q.title(),"text",flags.contains("npc:done:"+q.id())?q.ending():q.request(),"accepted",flags.contains("npc:accepted:"+q.id()),"complete",flags.contains("npc:done:"+q.id()),"owned",q.material().owned(flags,0),"amount",q.amount(),"coins",q.coins())).toList();}
 public static String command(String action,String id,Player p,Set<String> flags,Set<String> cleared){Quest q=ALL.stream().filter(x->x.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown request."));CoopDungeon.require(SubArea.get(q.route()).unlocked(flags,cleared),"Open this resident's story route first.");CoopDungeon.require(!flags.contains("npc:done:"+id),"This request is already complete.");
  if(action.equals("npc_accept")){CoopDungeon.require(flags.add("npc:accepted:"+id),"This request is already accepted.");return q.speaker()+": "+q.request();}
  CoopDungeon.require(flags.contains("npc:accepted:"+id),"Speak to the resident and accept their request first.");CoopDungeon.require(q.material().owned(flags,0)>=q.amount(),"Gather the requested materials first.");CoopDungeon.require(p.getCoins()<=Integer.MAX_VALUE-q.coins(),"Your coin pouch is full.");q.material().consume(flags,0,q.amount());p.addCoins(q.coins());flags.add("npc:done:"+id);return q.ending()+" +"+q.coins()+" coins.";
 }
}
