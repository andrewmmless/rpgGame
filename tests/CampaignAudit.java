import java.util.*;
public class CampaignAudit {
 @SuppressWarnings("unchecked") public static void main(String[] args){
  GameSession g=new GameSession(PlayerClass.valueOf(args.length==0?"WARRIOR":args[0]).create("Audit"),new Random(41));int turns=0,failures=0;List<String> rows=new ArrayList<>();
  for(SubArea route:SubArea.ALL){
   if(!g.snapshot().mode().equals("TOWN"))g.command("town","");
   g.command("rest","");while(g.snapshot().player().potions()<3&&g.snapshot().player().coins()>=8)g.command("buy_potion","");
   Map<String,Object> build=(Map<String,Object>)g.view().get("build");int points=(int)build.get("points");for(int i=0;i<points;i++)g.command("attribute",new String[]{"POWER","VITALITY","ARMOUR"}[i%3]);g.command("rest","");
   for(Equipment.Slot slot:Equipment.Slot.values()){var best=g.snapshot().inventory().stream().filter(e->e.slot()==slot).max(Comparator.comparingInt(Equipment::power));if(best.isPresent())g.command("equip",best.get().id());}
   for(Equipment e:List.copyOf(g.snapshot().inventory()))if(!g.snapshot().equipped().containsValue(e.id()))g.command("sell",e.id());
   if(StoryConsequences.available(g.snapshot().claimed(),g.snapshot().cleared()))g.command("story_choice","protect");
   int entry=g.snapshot().player().level(),start=turns;g.command("adventure",route.area().name()+":"+route.index());
   for(int step=0;step<180&&!g.snapshot().mode().equals("COMPLETE");step++){
    String mode=g.snapshot().mode();if(mode.equals("DEFEAT")){failures++;break;}
    if(mode.equals("TRAIL"))g.command("continue","");else if(mode.equals("SHRINE"))g.command("spring","");else if(mode.equals("OBJECTIVE"))g.command("objective","");else if(mode.equals("COMBAT")){
     Map<String,Object> v=g.view(),p=(Map<String,Object>)v.get("player"),e=(Map<String,Object>)v.get("enemy"),o=(Map<String,Object>)v.get("missionObjective");List<Map<String,Object>> abs=(List<Map<String,Object>>)v.get("abilities");
     Set<String> ready=new HashSet<>();for(var a:abs)if((int)a.get("cooldown")==0&&(int)a.get("cost")<=(int)p.get("resource"))ready.add((String)a.get("id"));
     int hp=(int)p.get("health"),max=(int)p.get("maxHealth");String action;
     if(hp<max*.65&&ready.contains("prayer"))action="ability:prayer";
     else if((int)p.get("resource")<25&&ready.contains("arcane_focus"))action="ability:arcane_focus";
     else if(hp<max*.65&&ready.contains("second_wind"))action="ability:second_wind";
     else if(hp<max*.35&&(int)p.get("potions")>0)action="POTION";
     else if(e.get("intent").toString().contains("Heavy"))action=ready.contains("shield_bash")?"ability:shield_bash":ready.contains("frostbolt")?"ability:frostbolt":ready.contains("pocket_sand")?"ability:pocket_sand":"DEFEND";
     else if(o!=null&&(int)o.get("progress")<2&&((int)o.get("integrity")<70||e.get("intent").toString().contains("Gathering")))action="OBJECTIVE";
     else if(ready.contains("meteor"))action="ability:meteor";else if(ready.contains("judgement"))action="ability:judgement";else if(ready.contains("deathmark"))action="ability:deathmark";else if(ready.contains("fireball"))action="ability:fireball";else if(ready.contains("smite"))action="ability:smite";else if(ready.contains("venom"))action="ability:venom";else if(ready.contains("backstab"))action="ability:backstab";else if(ready.contains("execution"))action="ability:execution";else if(ready.contains("ko_slash"))action="ability:ko_slash";else action="ATTACK";
     g.command("combat",action);turns++;
    }else throw new IllegalStateException(mode);
   }
   rows.add(route.name()+": entry level "+entry+", exit "+g.snapshot().player().level()+", combat turns "+(turns-start)+", "+g.snapshot().mode());
   if(!g.snapshot().mode().equals("COMPLETE")){System.out.println("STOP: "+g.snapshot().log().subList(Math.max(0,g.snapshot().log().size()-5),g.snapshot().log().size()));break;}
  }
  for(String row:rows)System.out.println(row);System.out.println("Total combat turns="+turns+" deaths="+failures);
 }
}
