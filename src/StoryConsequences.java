import java.util.*;
/** One authored decision with consequences at later campaign milestones. */
public final class StoryConsequences {
 private StoryConsequences(){}
 public static int catchupXp(Player p,Set<String> flags,Set<String> cleared){int target=1;for(SubArea r:SubArea.ALL)if(r.complete(flags,cleared))target=Math.max(target,r.index()==11?60:SubArea.get(r.index()+1).minLevel());if(p.getLevel()>=target)return 0;int xp=-p.getXp();for(int n=p.getLevel();n<target;n++)xp+=Balance.xpNeeded(n);return Math.max(0,xp);}
 public static String choice(Set<String> flags){return flags.contains("story:verdict:protect")?"protect":flags.contains("story:verdict:reveal")?"reveal":"";}
 public static boolean available(Set<String> flags,Set<String> cleared){return SubArea.get(4).complete(flags,cleared)&&choice(flags).isEmpty();}
 public static void choose(String value,Set<String> flags,Set<String> cleared){CoopDungeon.require(available(flags,cleared),"The sealed order decision is not available or has already been made.");CoopDungeon.require(Set.of("reveal","protect").contains(value),"Choose how to handle Cedric's evidence.");flags.add("story:verdict:"+value);}
 public static String ending(int route,String original,Set<String> flags){if(route==4)original="You recover the sealed order. Cedric asks for time to identify his family’s accomplices. Elin calls you back to the capital: will you reveal the order now or keep the inquiry sealed?";String c=choice(flags);if(c.isEmpty())return original;
  return original+switch(route){
   case 5->c.equals("reveal")?" Elin opens a public inquiry. Cedric surrenders his family seal; the rescued miners agree to testify.":" You persuade Elin to keep the inquiry sealed while Cedric identifies his family's accomplices. He earns no pardon, only time to help.";
   case 8->c.equals("reveal")?" The miners' public testimony exposes the archive's falsified accounts. The evidence will be heard in open court.":" Cedric's inside contact leads you to a hidden witness. You must secure that testimony before the court can bury it.";
   case 11->c.equals("reveal")?" After the victory, a public tribunal strips the guilty houses of command. Cedric serves the frontier under the miners' watch.":" Cedric testifies against his own house. The sealed inquiry becomes public at last, and the witness you protected names those responsible.";
   default->"";};
 }
 public static MissionObjective objective(int route,Set<String> flags){return route==8&&choice(flags).equals("protect")?MissionObjective.ESCORT:MissionObjective.forRoute(route);}
 public static Map<String,Object> view(Set<String> flags,Set<String> cleared){return Map.of("available",available(flags,cleared),"choice",choice(flags),"title","Cedric's sealed order","text","His family's crest is on the order. Reveal it now to secure public testimony, or keep the inquiry sealed while he locates a witness. This choice cannot be reversed. Protecting the witness changes the Drowned Archive mission to an escort; revealing the order keeps its ward-disruption mission.");}
}
