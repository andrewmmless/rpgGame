import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class ProfessionProgressionTest {
 @Test void allProfessionsGuaranteeDiscoveriesOnlyOnce(){for(Gathering g:Gathering.values()){Player p=new Mage("A");p.setLevel(60);Set<String> flags=new HashSet<>(),cleared=new HashSet<>();for(Area a:Area.values())cleared.add(a.name());for(int i=0;i<3;i++){p.setResource(p.getMaxResource());g.gather(0,p,flags,cleared,new Random(1),true);}assertEquals(15,p.getCoins());p.setResource(p.getMaxResource());g.gather(0,p,flags,cleared,new Random(1),true);assertEquals(15,p.getCoins());assertTrue(flags.contains("gather:"+g.name()+":find:0"));}}
 @Test void toolsHaveAtomicCostsAndIncreaseYield(){Player p=new Mage("A");p.setCoins(20);Set<String> flags=new HashSet<>(Set.of("gather:FORAGING:xp:100","gather:FORAGING:item0:7"));assertThrows(IllegalArgumentException.class,()->Gathering.FORAGING.upgrade(p,flags,Set.of()));assertEquals(20,p.getCoins());flags.remove("gather:FORAGING:item0:7");flags.add("gather:FORAGING:item0:8");Gathering.FORAGING.upgrade(p,flags,Set.of());assertEquals(0,p.getCoins());assertEquals(1,Gathering.FORAGING.tool(flags));Gathering.FORAGING.gather(0,p,flags,Set.of(),new Random(1),true);assertEquals(2,Gathering.FORAGING.owned(flags,0));assertEquals(28,p.getResource());}
}
