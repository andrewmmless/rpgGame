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
        new Decoration("campaign_banner","Cindergard campaign standard","wall",0,"archive"),new Decoration("plain_rug","Woven rug","floor",0,""),new Decoration("camp_lantern","Camp lantern","hearth",0,""),
        new Decoration("blue_rug","River-blue rug","floor",30,""),new Decoration("fireplace","Stone fireplace","hearth",60,""),
        new Decoration("books","Adventurers' bookshelf","shelf",40,""),new Decoration("flowers","Window flowers","garden",25,""),
        new Decoration("banner","Guild banner","wall",35,""),new Decoration("warden_trophy","Warden antlers","wall",0,"warden"),
        new Decoration("bond_lantern","Twin lanterns","hearth",0,"bond"));
    public record Project(String id,String name,int coins,int supplies,String benefit) {}
    public static final List<Project> PROJECTS=List.of(new Project("infirmary","Company infirmary",100,3,"Guildmates finishing co-op together recover 15% health."),new Project("workshop","Shared workshop",150,4,"Credited guild co-op clears deliver two supplies instead of one."),new Project("archive","Campaign archive",200,5,"Unlocks a permanent guild campaign banner."));
    public void build(String id,String actor){Project p=PROJECTS.stream().filter(x->x.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown project."));CoopDungeon.require(upgrade>=1,"Improve the house once before adding rooms.");CoopDungeon.require(!owned.contains("project:"+id),"This project is already complete.");CoopDungeon.require(coins>=p.coins()&&materials>=p.supplies(),"Contribute more guild coins and adventure supplies.");coins-=p.coins();materials-=p.supplies();owned.add("project:"+id);if(id.equals("archive"))owned.add("campaign_banner");record(actor+" completed "+p.name()+".");}
    public void transfer(String actor,String nextOwner,List<String> members){
        CoopDungeon.require(Objects.equals(actor,owner),"Only the guild leader can transfer leadership.");
        CoopDungeon.require(!actor.equals(nextOwner)&&members.contains(nextOwner),"Choose another current guild member.");
        owner=nextOwner;record(actor+" transferred guild leadership to "+nextOwner+".");
    }
    public void record(String text){history.add(text);while(history.size()>40)history.remove(0);}
    public static Decoration decoration(String id){return CATALOG.stream().filter(d->d.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown decoration."));}
}
