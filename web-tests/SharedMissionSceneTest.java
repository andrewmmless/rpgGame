import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SharedMissionSceneTest {
 @Test void firstChapterHasDistinctScenesAndAConclusion(){
  for(int route=0;route<3;route++){
   CoopDungeon p=new CoopDungeon();p.selectStory(route);p.state="BATTLE";p.enemyHealth=50;
   var titles=new java.util.HashSet<>();
   for(int stage=0;stage<3;stage++){p.stage=stage;var scene=SharedMissionScene.view(p);titles.add(scene.get("title"));assertFalse(scene.get("text").toString().isBlank());assertEquals(1,scene.get("chapter"));}
   assertEquals(3,titles.size());p.state="VICTORY";assertEquals(SubArea.get(route).ending(),SharedMissionScene.view(p).get("text"));
   if(route==2){assertTrue(SharedMissionScene.view(p).get("title").toString().contains("Chapter one complete"));assertTrue(SharedMissionScene.view(p).get("next").toString().contains("Entrance Tunnels"));}
  }
 }
 @Test void guidanceRespondsToDangerAndRemainingWork(){CoopDungeon p=new CoopDungeon();p.selectStory(0);p.state="BATTLE";p.enemyHealth=10;p.integrity=30;assertTrue(SharedMissionScene.view(p).get("advice").toString().contains("danger"));p.enemyHealth=0;assertTrue(SharedMissionScene.view(p).get("advice").toString().contains("enemy is defeated"));p.selectStory(1);p.enemyHealth=10;p.objectiveProgress=3;assertTrue(SharedMissionScene.view(p).get("advice").toString().contains("Objective complete"));}
 @Test void standaloneDungeonAndFinalRouteHaveSafeViews(){CoopDungeon p=new CoopDungeon();assertTrue(SharedMissionScene.view(p).isEmpty());p.selectStory(11);p.state="VICTORY";assertTrue(SharedMissionScene.view(p).get("next").toString().contains("campaign is complete"));}
}
