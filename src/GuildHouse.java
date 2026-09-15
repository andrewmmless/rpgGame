import java.util.*;
/** Small private guild; versioned social state, separate from character saves. */
public class GuildHouse {
    public static final int MAX_MEMBERS=8;
    public String name,owner,emblem="leaf",invite;
    public long version;
    public int coins,materials,upgrade;
    public Set<String> owned=new HashSet<>(Set.of("plain_rug","camp_lantern"));
    public Map<String,String> placed=new HashMap<>(Map.of("floor","plain_rug","hearth","camp_lantern"));
    public List<String> history=new ArrayList<>(),guestbook=new ArrayList<>();
    public Map<String,Long> reactions=new HashMap<>();
    public record Decoration(String id,String name,String slot,int price,String requirement) {}
    public static final List<Decoration> CATALOG=List.of(
        new Decoration("plain_rug","Woven rug","floor",0,""),new Decoration("camp_lantern","Camp lantern","hearth",0,""),
        new Decoration("blue_rug","River-blue rug","floor",30,""),new Decoration("fireplace","Stone fireplace","hearth",60,""),
        new Decoration("books","Adventurers' bookshelf","shelf",40,""),new Decoration("flowers","Window flowers","garden",25,""),
        new Decoration("banner","Guild banner","wall",35,""),new Decoration("warden_trophy","Warden antlers","wall",0,"warden"),
        new Decoration("bond_lantern","Twin lanterns","hearth",0,"bond"));
    public void transfer(String actor,String nextOwner,List<String> members){
        CoopDungeon.require(Objects.equals(actor,owner),"Only the guild leader can transfer leadership.");
        CoopDungeon.require(!actor.equals(nextOwner)&&members.contains(nextOwner),"Choose another current guild member.");
        owner=nextOwner;record(actor+" transferred guild leadership to "+nextOwner+".");
    }
    public void record(String text){history.add(text);while(history.size()>40)history.remove(0);}
    public static Decoration decoration(String id){return CATALOG.stream().filter(d->d.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown decoration."));}
}
