import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CoopScalingTest {
 private GameSave save(PlayerClass type,int level,boolean unlocked){Player p=type.create(type.name());p.setLevel(level);p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());GameSave s=new GameSession(p,new Random(1)).snapshot();Equipment gear=new Equipment("w","Test weapon",Equipment.Slot.WEAPON,Equipment.Rarity.RARE,level,5+level/2,0);Set<String> flags=new HashSet<>();if(unlocked)for(int i=0;i<6;i++)flags.add(SubArea.get(i).key());return new GameSave(1,s.player(),"TOWN",s.area(),0,0,0,0,0,Set.of(),flags,List.of(gear),Map.of(Equipment.Slot.WEAPON,"w"),List.of(),null);}
 @Test void levelSixtyCanJoinLevelOneWithoutOverpoweringOrChangingTheirSave(){CoopDungeon p=new CoopDungeon();GameSave high=save(PlayerClass.WARRIOR,60,false);p.add("a",high);p.add("b",save(PlayerClass.CLERIC,1,false));p.start("a",0);var member=p.members.get(0);assertEquals(1,member.player().getLevel());assertTrue(member.player().getAttackPower()<25);assertEquals(high.player().health(),member.restoredHealth());assertEquals(high.player().resource(),member.restoredResource());assertEquals(high,member.original);member.health=0;assertEquals(0,member.restoredHealth());var mapper=tools.jackson.databind.json.JsonMapper.builder().build();CoopDungeon restored=mapper.readValue(mapper.writeValueAsString(p),CoopDungeon.class);assertEquals(1,restored.members.get(0).syncedLevel);assertEquals(high,restored.members.get(0).original);}
 @Test void contractsRejectStorySkippingAndCapRepeatFarming(){CoopDungeon p=new CoopDungeon();p.contractId="ARCHIVE";assertThrows(IllegalArgumentException.class,()->p.add("a",save(PlayerClass.WARRIOR,60,false)));p.add("a",save(PlayerClass.WARRIOR,60,true));p.add("b",save(PlayerClass.CLERIC,60,true));p.start("a",0);assertEquals(40,p.level);assertEquals(40,p.members.get(0).player().getLevel());assertTrue(p.regionalBoss());assertTrue(p.intent().contains("ritual"));}
 @Test void mixedLevelPartiesCanFinishBothNewContracts(){for(String id:List.of("FORGE","ARCHIVE")){CoopDungeon party=new CoopDungeon();party.contractId=id;int target=party.contract().maxLevel();party.add("a",save(PlayerClass.WARRIOR,60,true));party.add("b",save(PlayerClass.CLERIC,target,true));party.start("a",0);
  for(int n=0;n<100&&party.state.equals("BATTLE");n++){int round=party.round;String[] actions=new String[2];for(int i=0;i<2;i++){var m=party.members.get(i);Player p=m.player();Set<String> ready=new HashSet<>();for(Ability a:p.getAvailableAbilities())if(a.cost()<=p.getResource()&&m.cooldowns.getOrDefault(a.id(),0)==0)ready.add(a.id());
   if(p.getHealth()<p.getMaxHealth()*.65&&ready.contains("prayer"))actions[i]="ability:prayer";
   else if(p.getHealth()<p.getMaxHealth()*.65&&ready.contains("second_wind"))actions[i]="ability:second_wind";
   else if(party.intent().contains("Heavy"))actions[i]="DEFEND";
   else if(ready.contains("judgement"))actions[i]="ability:judgement";else if(ready.contains("execution"))actions[i]="ability:execution";else if(ready.contains("smite"))actions[i]="ability:smite";else if(ready.contains("ko_slash"))actions[i]="ability:ko_slash";else actions[i]="ATTACK";
  }party.choose("a",round,actions[0],n,new Random(n));party.choose("b",round,actions[1],n,new Random(n));}
  assertEquals("VICTORY",party.state,id+": "+party.log);
 }}
}
