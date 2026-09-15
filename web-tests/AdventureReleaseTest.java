import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class AdventureReleaseTest {
 @Test void bossesHaveDistinctOpeningsAndPredictablePhases(){
  String[] names={"Alpha Wolf","Orc Chieftain","Lich Acolyte","Ashwing, the Red Wyrm"};
  Set<Enemy.Move> openings=new HashSet<>();
  for(String name:names){Enemy e=new Enemy(name,15,200,20,5,0,0,0,0).asBoss();openings.add(e.move(0));assertEquals(Enemy.Move.ATTACK,e.move(3));e.restoreHealth(100);assertEquals(Enemy.Move.DRAIN,e.move(3));assertEquals(Enemy.Move.CHARGE,e.move(1));assertEquals(Enemy.Move.HEAVY,e.move(2));}
  assertEquals(4,openings.size());
 }
 @Test void guildProjectsAreAtomicAndCannotBeBoughtTwice(){
  GuildHouse g=new GuildHouse();g.upgrade=1;g.coins=200;g.materials=2;
  assertThrows(IllegalArgumentException.class,()->g.build("infirmary","A"));assertEquals(200,g.coins);assertEquals(2,g.materials);
  g.materials=3;g.build("infirmary","A");assertEquals(100,g.coins);assertEquals(0,g.materials);
  assertThrows(IllegalArgumentException.class,()->g.build("infirmary","A"));assertEquals(100,g.coins);
 }
 @Test void crownwardHasTradeoffAndDefenceRecovery(){
  Player p=new Mage("A");p.setHealth(20);p.setResource(0);Enemy e=new Enemy("Target",1,100,1,0,0,0,0,0);
  WeaponAttribute.CROWNWARD.apply(CombatAction.ABILITY,5,p,e,new ArrayList<>());assertEquals(18,p.getHealth());assertEquals(96,e.getHealth());
  WeaponAttribute.CROWNWARD.apply(CombatAction.DEFEND,0,p,e,new ArrayList<>());assertTrue(p.getHealth()>18);assertEquals(6,p.getResource());
 }
 @Test void scoutingChoicePersistsAndCannotBeRepeated(){
  GameSession g=new GameSession(new Mage("A"),new Random(1));GameSave s=g.snapshot();
  g=GameSession.restore(new GameSave(1,s.player(),"SHRINE",Area.WHISPERING_WOODS,2,0,0,0,0,Set.of(),Set.of("route:active:0","mission:enabled"),s.inventory(),s.equipped(),s.log(),null),new Random(1));
  g.command("road_choice","scout");assertEquals("TRAIL",g.snapshot().mode());assertTrue(g.snapshot().claimed().contains("mission:scout"));
  GameSession restored=GameSession.restore(g.snapshot(),new Random(1));assertThrows(IllegalArgumentException.class,()->restored.command("road_choice","scout"));restored.command("continue","");assertTrue(restored.snapshot().battle().playerStatuses().stream().anyMatch(x->x.effect().id().equals("scouted")));
 }
 @Test void coOpBossWarningsAndGuardDoNotLeakIntoNextRound(){
  CoopDungeon party=new CoopDungeon();party.selectStory(2);
  Player player=new Warrior("A");player.setLevel(60);player.setHealth(player.getMaxHealth());GameSave base=new GameSession(player,new Random(1)).snapshot();
  GameSave save=new GameSave(1,base.player(),"TOWN",base.area(),0,0,0,0,0,Set.of(),Set.of(SubArea.get(0).key(),SubArea.get(1).key()),base.inventory(),base.equipped(),base.log(),null);
  party.add("a",save);party.add("b",save);party.start("a",0);party.stage=2;party.round=2;party.enemyHealth=party.enemyMaxHealth;
  assertTrue(party.intent().contains("Heavy"));party.choose("a",2,"DEFEND",1,new Random(1));party.choose("b",2,"PROTECT",1,new Random(1));
  assertEquals(3,party.round);assertTrue(party.members.stream().allMatch(m->m.statuses.stream().noneMatch(st->st.effect().id().equals("party-guard"))));
 }
 @Test void sharedApproachSurvivesSaveAndCostsBothPlayersOnce(){
  CoopDungeon party=new CoopDungeon();party.selectStory(0);GameSave save=new GameSession(new Mage("A"),new Random(1)).snapshot();party.add("a",save);party.add("b",save);
  assertThrows(IllegalArgumentException.class,()->party.chooseApproach("b","rescue"));party.chooseApproach("a","rescue");
  var json=tools.jackson.databind.json.JsonMapper.builder().build();CoopDungeon restored=json.readValue(json.writeValueAsString(party),CoopDungeon.class);int resource=restored.members.get(0).resource;restored.start("a",0);
  assertEquals(resource-12,restored.members.get(0).resource);assertEquals(resource-12,restored.members.get(1).resource);assertTrue(restored.coinReward()>40);assertThrows(IllegalArgumentException.class,()->restored.start("a",0));
 }
}
