import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class BossMaterialsTest {
    @Test void onlyRegionalFinalesAwardPersistentMaterials(){
        Set<String> flags=new HashSet<>();
        for(int route=0;route<12;route++)BossMaterials.award(flags,route);
        for(int region=0;region<4;region++)assertEquals(1,BossMaterials.owned(flags,region));
        Player p=new Mage("Hunter");GameSession session=new GameSession(p,new Random(1));
        GameSave s=session.snapshot();
        GameSave saved=new GameSave(1,s.player(),s.mode(),s.area(),0,0,0,0,0,Set.of(),flags,s.inventory(),s.equipped(),s.log(),null);
        assertEquals(flags,GameSession.restore(saved,new Random(1)).snapshot().claimed());
    }
    @Test void legendaryRequiresCompletionAndAllMaterialsBeforeSpending(){
        Player p=new Mage("Smith");p.setLevel(60);p.setCoins(1000);
        Set<String> flags=new HashSet<>(Set.of("gather:MINING:item2:20","gather:FORAGING:item2:20"));
        for(int region=0;region<4;region++)for(int n=0;n<(region==3?3:2);n++)BossMaterials.award(flags,region*3+2);
        Set<String> before=Set.copyOf(flags);
        assertThrows(IllegalArgumentException.class,()->Crafting.craft("crown_weapon",p,flags,Set.of(),0));
        assertEquals(before,flags);assertEquals(1000,p.getCoins());
        Set<String> cleared=Set.of(Area.DRAGONS_SPIRE.name());
        assertThrows(IllegalArgumentException.class,()->Crafting.craft("crown_weapon",p,flags,cleared,30));
        assertEquals(before,flags);
        Equipment item=Crafting.craft("crown_weapon",p,flags,cleared,0);
        assertEquals(Equipment.Rarity.LEGENDARY,item.rarity());assertEquals(41,item.power());assertTrue(item.name().contains("Staff"));
        assertEquals(650,p.getCoins());assertEquals(0,BossMaterials.owned(flags,3));
        assertThrows(IllegalArgumentException.class,()->Crafting.craft("crown_weapon",p,flags,cleared,0));assertEquals(650,p.getCoins());
        for(PlayerClass type:PlayerClass.values())assertFalse(Equipment.weaponName(type,Equipment.Rarity.LEGENDARY).isBlank());
    }
}
