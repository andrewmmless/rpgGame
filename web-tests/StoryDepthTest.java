import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class StoryDepthTest {
 @Test void decisionIsGatedPermanentAndChangesLaterMission(){
  Set<String> flags=new HashSet<>();assertThrows(IllegalArgumentException.class,()->StoryConsequences.choose("protect",flags,Set.of()));flags.add(SubArea.get(4).key());StoryConsequences.choose("protect",flags,Set.of());
  assertEquals(MissionObjective.ESCORT,StoryConsequences.objective(8,flags));assertThrows(IllegalArgumentException.class,()->StoryConsequences.choose("reveal",flags,Set.of()));assertTrue(StoryConsequences.ending(11,"Victory.",flags).contains("testifies"));
 }
 @Test void legendaryArmourRewardsGuardAndPunishesUnguardedHits(){
  Player normal=new Warrior("Normal"),armoured=new Warrior("Armoured");armoured.equipCrownArmour(true);normal.setResource(0);armoured.setResource(0);
  assertTrue(armoured.receiveDamage(30,DamageType.PHYSICAL)>normal.receiveDamage(30,DamageType.PHYSICAL));
  normal.applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));armoured.applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));assertTrue(armoured.receiveDamage(30,DamageType.PHYSICAL)<normal.receiveDamage(30,DamageType.PHYSICAL));assertEquals(2,armoured.getResource());
 }
 @Test void countersRequireTheAdvertisedAction(){
  Enemy lich=new Enemy("Lich Acolyte",40,400,50,40,0,0,0,0).asBoss();assertFalse(BossCounters.interrupt(lich,Enemy.Move.RECOVER,false,20,new ArrayList<>()));assertTrue(BossCounters.interrupt(lich,Enemy.Move.RECOVER,true,20,new ArrayList<>()));
  Enemy wolf=new Enemy("Alpha Wolf",10,200,20,10,0,0,0,0).asBoss();Player p=new Warrior("Guard");p.applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));wolf.performTurn(0,p,new Random(1),new ArrayList<>());assertFalse(p.hasStatus(StatusEffect.Kind.DAMAGE_OVER_TIME));
 }
 @Test void earnedRewardSummarySurvivesReloadWithoutNewRewards(){
  Player p=new Warrior("A");p.setLevel(15);p.setHealth(p.getMaxHealth());GameSave base=new GameSession(p,new Random(1)).snapshot();Set<String> flags=new HashSet<>(Set.of("route:active:2","mission:enabled","mission:ledger","mission:work:2",SubArea.get(1).key()));
  GameSave.BattleData battle=new GameSave.BattleData("Alpha Wolf",10,100,1,20,10,true,1,Map.of(),List.of(),List.of());GameSession g=GameSession.restore(new GameSave(1,base.player(),"COMBAT",Area.WHISPERING_WOODS,4,0,0,0,0,Set.of(),flags,List.of(),Map.of(),List.of(),battle),new Random(1));
  g.command("combat","ATTACK");assertEquals(GameSession.Mode.COMPLETE,g.view().get("mode"));var summary=(Map<?,?>)g.view().get("expeditionRewards");assertTrue((int)summary.get("xp")>0);assertTrue((int)summary.get("coins")>0);assertEquals(1,summary.get("trophyOwned"));assertEquals(1,((List<?>)summary.get("items")).size());GameSession restored=GameSession.restore(g.snapshot(),new Random(1));assertEquals(summary,restored.view().get("expeditionRewards"));assertThrows(IllegalArgumentException.class,()->restored.command("objective",""));
 }
 @Test void storyXpClosesTheNextRouteGapWithoutChangingRepeatRewards(){
  Player p=new Mage("A");p.setLevel(3);p.setXp(10);p.gainXp(Balance.storyClearXp(3,10,0));assertTrue(p.getLevel()>=5);
  p.setLevel(56);p.setXp(0);p.gainXp(Balance.storyClearXp(56,0,11));assertTrue(p.getLevel()>=60);assertEquals(100,Balance.storyClearXp(10,0,0));
  assertEquals(2,SubArea.get(0).encounter(4,1,new Random(1)).getLevel());assertEquals(15,SubArea.get(2).encounter(4,10,new Random(1)).getLevel());
 }
 @Test void equippedArmourEffectIsReconstructedForCoop(){
  GameSave s=new GameSession(new Warrior("A"),new Random(1)).snapshot();Equipment armour=new Equipment("crown","Crownfire plate",Equipment.Slot.ARMOUR,Equipment.Rarity.LEGENDARY,60,41,0);
  GameSave saved=new GameSave(1,s.player(),"TOWN",s.area(),0,0,0,0,0,Set.of(),Set.of(),List.of(armour),Map.of(Equipment.Slot.ARMOUR,"crown"),List.of(),null);
  Player reconstructed=new CoopDungeon.Member("a",saved).player();Player ordinary=new Warrior("B");ordinary.equipBonuses(0,41);assertTrue(reconstructed.receiveDamage(40,DamageType.PHYSICAL)>ordinary.receiveDamage(40,DamageType.PHYSICAL));
 }
 @Test void legacyStoryCatchupCannotBeFarmed(){Player p=new Mage("Old save");p.setLevel(44);Set<String> cleared=Set.of(Area.DRAGONS_SPIRE.name());int xp=StoryConsequences.catchupXp(p,Set.of(),cleared);assertTrue(xp>0);p.gainXp(xp);assertEquals(60,p.getLevel());assertEquals(0,StoryConsequences.catchupXp(p,Set.of(),cleared));}
}
