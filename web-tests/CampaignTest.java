import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class CampaignTest {
    @Test void campaignSaveSurvivesEveryStepAndRewardsCannotRepeat() throws Exception {
        Player p=new Mage("Campaign");p.setLevel(30);p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());
        GameSession game=new GameSession(p,new Random(4));ObjectMapper json=JsonMapper.builder().build();
        for(Area area:Area.values()) {
            game.command("rest","");game.command("adventure",area.name());
            int limit=0;
            while(!game.snapshot().mode().equals("COMPLETE")&&limit++<100) {
                switch(game.snapshot().mode()) {
                    case "TRAIL" -> game.command("continue","");
                    case "SHRINE" -> game.command("spring","");
                    case "COMBAT" -> game.command("combat","ATTACK");
                    default -> fail("Unexpected mode "+game.snapshot().mode());
                }
                String saved=json.writeValueAsString(game.snapshot());
                game=GameSession.restore(json.readValue(saved,GameSave.class),new Random(limit));
                assertEquals(saved,json.writeValueAsString(game.snapshot()),"Round-trip should preserve all persisted state");
            }
            assertTrue(limit<100);assertTrue(game.snapshot().cleared().contains(area.name()));
            game.command("claim",area.name());
            GameSession current=game;assertThrows(IllegalArgumentException.class,()->current.command("claim",area.name()));
            game.command("town","");
        }
        assertEquals(4,game.snapshot().cleared().size());
        game.command("tower","");assertEquals(1,game.snapshot().towerFloor());
    }
    @Test void commandsRespectModeAndAreaGates() {
        GameSession game=new GameSession(new Warrior("Test"),new Random(1));
        assertThrows(IllegalArgumentException.class,()->game.command("adventure","STONEFANG_CAVES"));
        assertThrows(IllegalArgumentException.class,()->game.command("claim","first_steps"));
        game.command("adventure","WHISPERING_WOODS");game.command("continue","");
        assertThrows(IllegalArgumentException.class,()->game.command("rest",""));
        assertThrows(IllegalArgumentException.class,()->game.command("equip","missing"));
        assertThrows(IllegalArgumentException.class,()->game.command("town",""));
        GameSave before=game.snapshot();assertThrows(IllegalArgumentException.class,()->game.command("combat","ability:missing"));
        assertEquals(before,game.snapshot());
    }
    @Test void equipmentChangesDamageAndDefenceAndUpgradesAreCapped() {
        Player p=new Warrior("Test");int attack=p.getAttackPower(),defence=p.getDefence();p.equipBonuses(5,7);
        assertEquals(attack+5,p.getAttackPower());assertEquals(defence+7,p.getDefence());
        Equipment item=new Equipment("one","Sword",Equipment.Slot.WEAPON,Equipment.Rarity.RARE,1,3,0);
        for(int i=0;i<5;i++)item=item.upgrade();assertEquals(13,item.power());
        Equipment max=item;assertThrows(IllegalArgumentException.class,max::upgrade);
    }
    @Test void fourClassesUnlockAllAbilitiesAndStunOnlySkipsOneEnemyTurn() {
        for(PlayerClass type:PlayerClass.values()) {
            Player player=type.create("Test");assertEquals(1,player.getAvailableAbilities().size());player.setLevel(8);assertEquals(4,player.getAvailableAbilities().size());
        }
        Player player=new Warrior("Test");player.setLevel(3);player.setHealth(player.getMaxHealth());
        Enemy enemy=new Enemy("Target",3,200,12,2,1,1,1,1);CombatEngine engine=new CombatEngine(player,enemy,new Random(1));
        engine.performAction(CombatAction.ABILITY,"shield_bash");assertEquals(player.getMaxHealth(),player.getHealth());assertTrue(enemy.getStatuses().isEmpty());
        engine.performAction(CombatAction.ATTACK);assertTrue(player.getHealth()<player.getMaxHealth());
    }
    @Test void bossTelegraphAndDefenceReduceHeavyStrike() {
        Player guarded=new Warrior("G"),plain=new Warrior("P");
        Enemy one=new Enemy("Boss",1,500,10,0,0,0,0,0).asBoss(),two=new Enemy("Boss",1,500,10,0,0,0,0,0).asBoss();
        CombatEngine guardEngine=new CombatEngine(guarded,one,new Random(4)),plainEngine=new CombatEngine(plain,two,new Random(4));
        guardEngine.performAction(CombatAction.ATTACK);plainEngine.performAction(CombatAction.ATTACK);
        assertEquals(100,plain.getHealth());assertTrue(one.intent(guardEngine.getRound()).contains("Heavy"));
        guardEngine.performAction(CombatAction.DEFEND);plainEngine.performAction(CombatAction.ATTACK);
        assertTrue(guarded.getHealth()>plain.getHealth());
    }
    @Test void aWholeStarterExpeditionIsSurvivableForAllClasses() {
        for(PlayerClass type:PlayerClass.values())for(int seed=0;seed<100;seed++) {
            GameSession game=new GameSession(type.create("Test"),new Random(seed));game.command("adventure","WHISPERING_WOODS");
            int steps=0;
            while(!Set.of("COMPLETE","DEFEAT").contains(game.snapshot().mode())&&steps++<120) {
                switch(game.snapshot().mode()) {
                    case "TRAIL" -> game.command("continue","");
                    case "SHRINE" -> game.command("spring","");
                    case "COMBAT" -> {
                        GameSave s=game.snapshot();
                        if(s.player().health()<s.player().type().health*0.4&&s.player().potions()>0)game.command("combat","POTION");
                        else if(s.battle().boss()&&s.battle().round()%3==1)game.command("combat","DEFEND");
                        else game.command("combat","ATTACK");
                    }
                    default -> fail();
                }
            }
            assertEquals("COMPLETE",game.snapshot().mode(),type+" seed "+seed);
            assertTrue(game.snapshot().inventory().size()>=1,"Boss guarantees equipment");
        }
    }
}
