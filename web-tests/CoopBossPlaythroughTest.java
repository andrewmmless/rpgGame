import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CoopBossPlaythroughTest {
 @Test void preparedMixedPartyCanCounterEachRegionalFinale(){
  for(int route:new int[]{2,5,8,11}){
   CoopDungeon party=new CoopDungeon();party.selectStory(route);Set<String> flags=new HashSet<>();for(int n=0;n<route;n++)flags.add(SubArea.get(n).key());if(route==8)flags.add("story:verdict:protect");
   for(int i=0;i<2;i++){Player p=(i==0?PlayerClass.WARRIOR:PlayerClass.CLERIC).create("Partner"+i);p.setLevel(SubArea.get(route).maxLevel());p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());GameSave s=new GameSession(p,new Random(1)).snapshot();Equipment weapon=new Equipment("weapon","Test weapon",Equipment.Slot.WEAPON,Equipment.Rarity.RARE,p.getLevel(),5+p.getLevel()/2,0);GameSave saved=new GameSave(1,s.player(),"TOWN",s.area(),0,0,0,0,0,Set.of(),flags,List.of(weapon),Map.of(Equipment.Slot.WEAPON,"weapon"),List.of(),null);party.add("p"+i,saved);}
   party.start("p0",0);party.stage=2;party.enemyMaxHealth=150+22*(party.level-1);party.enemyHealth=party.enemyMaxHealth;
   for(int n=0;n<60&&party.state.equals("BATTLE");n++){int round=party.round;String[] choices=new String[2];for(int i=0;i<2;i++){var member=party.members.get(i);Player p=member.player();Set<String> ready=new HashSet<>();for(Ability a:p.getAvailableAbilities())if(a.cost()<=p.getResource()&&member.cooldowns.getOrDefault(a.id(),0)==0)ready.add(a.id());
     if(p.getHealth()<p.getMaxHealth()/2&&ready.contains("prayer"))choices[i]="ability:prayer";
     else if(p.getHealth()<p.getMaxHealth()/2&&ready.contains("second_wind"))choices[i]="ability:second_wind";
     else if(party.intent().contains("Heavy"))choices[i]="DEFEND";
     else if(i==0&&(party.objectiveProgress<3||party.integrity<65))choices[i]="OBJECTIVE";
     else if(ready.contains("judgement"))choices[i]="ability:judgement";else if(ready.contains("execution"))choices[i]="ability:execution";else if(ready.contains("smite"))choices[i]="ability:smite";else if(ready.contains("ko_slash"))choices[i]="ability:ko_slash";else choices[i]="ATTACK";
    }party.choose("p0",round,choices[0],n,new Random(n));party.choose("p1",round,choices[1],n,new Random(n));}
   assertEquals("VICTORY",party.state,"Regional finale "+route+": "+party.log);assertFalse(party.lastRoundEvents.isEmpty());
  }
 }
}
