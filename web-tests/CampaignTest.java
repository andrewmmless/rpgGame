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
    @Test void milestoneChestsPersistAndCannotBeRerolledOrClaimedEarly() {
        GameSession fresh=new GameSession(new Mage("Chest"),new Random(1));
        assertThrows(IllegalArgumentException.class,()->fresh.command("open_chest",""));
        GameSave s=fresh.snapshot();
        GameSave earned=new GameSave(s.schemaVersion(),s.player(),s.mode(),s.area(),s.room(),3,s.deaths(),s.towerBest(),s.towerFloor(),s.cleared(),s.claimed(),s.inventory(),s.equipped(),s.log(),s.battle());
        GameSession game=GameSession.restore(earned,new Random(1));
        assertEquals(1,game.view().get("chests"));
        game.command("open_chest","EPIC");
        assertEquals(1,game.snapshot().inventory().size());assertEquals(0,game.view().get("chests"));
        GameSave saved=game.snapshot();GameSession restored=GameSession.restore(saved,new Random(99));
        assertThrows(IllegalArgumentException.class,()->restored.command("open_chest",""));
        assertEquals(saved,restored.snapshot(),"A rejected reroll must preserve the original reward");
        List<Equipment> full=new ArrayList<>();for(int n=0;n<30;n++)full.add(new Equipment("item"+n,"Sword",Equipment.Slot.WEAPON,Equipment.Rarity.COMMON,1,2,0));
        GameSave fullSave=new GameSave(s.schemaVersion(),s.player(),s.mode(),s.area(),s.room(),3,s.deaths(),s.towerBest(),s.towerFloor(),s.cleared(),s.claimed(),full,s.equipped(),s.log(),s.battle());
        GameSession fullGame=GameSession.restore(fullSave,new Random(1));
        assertThrows(IllegalArgumentException.class,()->fullGame.command("open_chest",""));
        assertEquals(1,fullGame.view().get("chests"));assertEquals(fullSave,fullGame.snapshot());
        GameSession onTrail=GameSession.restore(earned,new Random(1));onTrail.command("adventure","WHISPERING_WOODS");
        assertThrows(IllegalArgumentException.class,()->onTrail.command("open_chest",""));
    }
    @Test void weaponAttributesWorkAndSurviveEquipmentSaveRoundTrips() throws Exception {
        Player p=new Mage("Attribute");p.setResource(10);p.equipAttribute(WeaponAttribute.FOCUS);
        Enemy target=new Enemy("Target",1,500,0,0,0,0,0,0);CombatEngine engine=new CombatEngine(p,target,new Random(1));
        engine.performAction(CombatAction.DEFEND);assertEquals(24,p.getResource()); // 10 initial + 6 defend + 4 Focus + 4 round recovery.
        p.equipAttribute(WeaponAttribute.SIPHON);p.setHealth(30);int hp=p.getHealth();
        CombatResult siphon=engine.performAction(CombatAction.ABILITY,"fireball");assertTrue(siphon.events().stream().anyMatch(e->e.startsWith("Siphon restored 2")));
        assertTrue(p.getHealth()>=hp);
        p.equipAttribute(WeaponAttribute.PIERCING);CombatResult piercing=engine.performAction(CombatAction.ATTACK);
        assertTrue(piercing.events().stream().anyMatch(e->e.startsWith("Piercing dealt 2")));
        Equipment sword=new Equipment("trait","Focus Blade",Equipment.Slot.WEAPON,Equipment.Rarity.RARE,1,3,0,WeaponAttribute.FOCUS);
        assertEquals(WeaponAttribute.FOCUS,sword.upgrade().attribute());
        JsonMapper json=JsonMapper.builder().build();assertEquals(sword,json.readValue(json.writeValueAsString(sword),Equipment.class));
        String legacy=json.writeValueAsString(sword).replace(",\"attribute\":\"FOCUS\"","");
        assertEquals(WeaponAttribute.NONE,json.readValue(legacy,Equipment.class).attribute());
        GameSave base=new GameSession(new Mage("Test"),new Random(1)).snapshot();
        GameSave save=new GameSave(1,base.player(),base.mode(),base.area(),0,0,0,0,0,Set.of(),Set.of(),List.of(sword),Map.of(Equipment.Slot.WEAPON,"trait"),base.log(),null);
        GameSession restored=GameSession.restore(save,new Random(1));assertEquals(save,restored.snapshot());
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
