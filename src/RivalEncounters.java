import java.util.*;
/** Optional story duels: no loot farming, loss penalty or campaign gate. */
public final class RivalEncounters {
 private RivalEncounters(){}
 public record Duel(int id,int route,int level,String title,String opening,String victory){}
 public static final List<Duel> ALL=List.of(
  new Duel(0,1,8,"A borrowed name","Cedric blocks the practice yard. ‘Elin trusts you with her company. Show me what she sees.’","Cedric lowers his blade. ‘Perhaps a family crest is not the same as earning a place.’"),
  new Duel(1,4,25,"The weight of the seal","The sealed order lies between you. ‘I cannot undo my family's signature,’ Cedric says. ‘But I can learn to stand beside you.’","‘Take the evidence to Elin,’ Cedric says. ‘I will answer her myself.’"),
  new Duel(2,7,40,"Someone worth protecting","Cedric asks for a final rehearsal before the archive. ‘If the dead reach the witnesses, I need to hold them. Don't make this easy.’","Cedric stays to drill with the watch. This time he asks the youngest recruit for advice."),
  new Duel(3,10,55,"No borrowed honour","At the last safe camp, Cedric draws a blunted sword. ‘One last bout. Tomorrow we stand against Ashwing as equals.’","Cedric clasps your arm. ‘Whatever the court calls us, you are my equal.’"));
 public static Duel get(int id){CoopDungeon.require(id>=0&&id<ALL.size(),"Choose a rival encounter.");return ALL.get(id);}
 public static String key(int id){return "rival:won:"+id;}
 public static List<Map<String,Object>> view(Set<String> flags,Set<String> cleared){return ALL.stream().map(d->Map.<String,Object>of("id",d.id(),"title",d.title(),"level",d.level(),"opening",flags.contains(key(d.id()))?d.victory():d.opening(),"unlocked",SubArea.get(d.route()).complete(flags,cleared)&&(d.id()==0||flags.contains(key(d.id()-1))),"complete",flags.contains(key(d.id())),"route",SubArea.get(d.route()).name(),"coins",20+d.id()*20)).toList();}
 public static Enemy enemy(Duel d){return new Enemy("Cedric Ashford · "+d.title(),d.level(),65+d.level()*8,8+d.level()*2,5+d.level(),0,0,0,0).asBoss();}
}
