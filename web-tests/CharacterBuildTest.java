import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import tools.jackson.databind.json.JsonMapper;
class CharacterBuildTest {
    private GameSession game(PlayerClass type,int level,Set<String> clears){Player p=type.create("Build");p.setLevel(level);p.setHealth(p.getMaxHealth());p.setResource(p.getMaxResource());GameSave s=new GameSession(p,new Random(1)).snapshot();return GameSession.restore(new GameSave(1,s.player(),s.mode(),s.area(),0,0,0,0,0,clears,s.claimed(),s.inventory(),s.equipped(),s.log(),null),new Random(1));}
    @Test void statsPersistInSoloAndCoopWithoutHealingOnAllocation(){
        GameSession g=game(PlayerClass.MAGE,3,Set.of());int health=g.snapshot().player().health();g.command("attribute","VITALITY");g.command("attribute","FOCUS");g.command("rest","");
        GameSave saved=g.snapshot();assertEquals(health+4,saved.player().health());GameSession restored=GameSession.restore(saved,new Random(1));assertEquals(saved,restored.snapshot());
        CoopDungeon party=new CoopDungeon();party.add("a",saved);Player member=party.members.get(0).player();assertEquals(saved.player().health(),member.getMaxHealth());assertEquals(saved.player().resource(),member.getMaxResource());
        g.command("attribute","POWER");g.command("attribute","ARMOUR");GameSave before=g.snapshot();assertThrows(IllegalArgumentException.class,()->g.command("attribute","POWER"));assertEquals(before,g.snapshot());
    }
    @Test void specialisationsRequirePromotionAndCorrectClass(){
        GameSession locked=game(PlayerClass.MAGE,20,Set.of());assertThrows(IllegalArgumentException.class,()->locked.command("specialise","BATTLE_MAGE"));
        GameSession g=game(PlayerClass.MAGE,20,Set.of("WHISPERING_WOODS"));assertThrows(IllegalArgumentException.class,()->g.command("specialise","VANGUARD"));g.command("specialise","SPELLWARDEN");g.command("rest","");assertEquals(98,g.snapshot().player().resource());
        g.command("loadout","signature_cinder,fireball");Player p=GameSession.restore(g.snapshot(),new Random(1)).snapshot().player().type().create("Plain");assertEquals(PlayerClass.MAGE,p.getPlayerClass());assertEquals(2,((List<?>)g.view().get("abilities")).size());
        assertThrows(IllegalArgumentException.class,()->g.command("loadout","trained_barrier"));assertThrows(IllegalArgumentException.class,()->g.command("loadout","fireball,fireball"));
    }
    @Test void upgradesAreLimitedAndLoadedAbilitiesEnforcedByCombat(){
        GameSession g=game(PlayerClass.MAGE,12,Set.of("WHISPERING_WOODS","STONEFANG_CAVES"));g.command("specialise","BATTLE_MAGE");
        for(int i=0;i<3;i++)g.command("upgrade_ability","fireball");assertThrows(IllegalArgumentException.class,()->g.command("upgrade_ability","fireball"));
        g.command("loadout","fireball,signature_cinder,trained_barrier");assertThrows(IllegalArgumentException.class,()->g.command("loadout","fireball,frostbolt,meteor,arcane_focus,signature_cinder"));
        g.command("adventure","WHISPERING_WOODS:0");g.command("continue","");assertThrows(IllegalArgumentException.class,()->g.command("combat","ability:frostbolt"));int resource=g.snapshot().player().resource();g.command("combat","ability:fireball");assertEquals(resource-11+4,g.snapshot().player().resource());
        assertThrows(IllegalArgumentException.class,()->g.command("reset_build",""));
    }
    @Test void resourceTrainingAndGearEffectsActuallyWork(){
        Player p=new Mage("Mage");p.setLevel(12);p.configureBuild(Set.of("build:skill:arcane_focus:3"),Set.of());p.setResource(0);
        Enemy target=new Enemy("Dummy",12,500,0,0,0,0,0,0);p.getAvailableAbilities().stream().filter(a->a.id().equals("arcane_focus")).findFirst().orElseThrow().effect().apply(p,target,new Random(1),new ArrayList<>());assertEquals(28,p.getResource());
        p.setResource(0);WeaponAttribute.CONDUIT.apply(CombatAction.ABILITY,5,p,target,new ArrayList<>());assertEquals(3,p.getResource());p.setHealth(20);WeaponAttribute.VAMPIRIC.apply(CombatAction.ATTACK,5,p,target,new ArrayList<>());assertEquals(23,p.getHealth());
    }
    @Test void comboCooldownSurvivesSerializationAndDoesNotRepeatNextRound(){
        CoopDungeon party=new CoopDungeon();party.add("a",game(PlayerClass.WARRIOR,20,Set.of()).snapshot());party.add("b",game(PlayerClass.MAGE,20,Set.of()).snapshot());party.start("a",0);
        party.choose("a",0,"PROTECT",1,new Random(1));party.choose("b",0,"ability:fireball",1,new Random(1));assertEquals(3,party.comboReadyRound);assertTrue(party.log.stream().anyMatch(l->l.contains("Shielded Inferno")));
        JsonMapper json=JsonMapper.builder().build();party=json.readValue(json.writeValueAsString(party),CoopDungeon.class);assertEquals(3,party.comboReadyRound);party.choose("a",1,"PROTECT",2,new Random(1));party.choose("b",1,"ability:frostbolt",2,new Random(1));assertEquals(1,party.log.stream().filter(l->l.contains("Shielded Inferno")).count());
    }
    @Test void sameClassAndUncoordinatedActionsCannotTriggerCombos(){Player[] same={new Mage("A"),new Mage("B")};assertFalse(DuoCombos.eligible(same,"ability:fireball","ability:fireball"));Player[] mixed={new Mage("A"),new Rogue("B")};assertFalse(DuoCombos.eligible(mixed,"ATTACK","ATTACK"));assertTrue(DuoCombos.eligible(mixed,"ability:fireball","ability:backstab"));}
}
