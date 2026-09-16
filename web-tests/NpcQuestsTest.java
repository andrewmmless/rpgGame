import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class NpcQuestsTest {
 @Test void requestsAreGatedConsumeMaterialsOnceAndKeepCompletion(){Player p=new Mage("A");Set<String> flags=new HashSet<>();assertThrows(IllegalArgumentException.class,()->NpcQuests.command("npc_accept","miners_lamps",p,flags,Set.of()));NpcQuests.command("npc_accept","warm_beds",p,flags,Set.of());assertThrows(IllegalArgumentException.class,()->NpcQuests.command("npc_deliver","warm_beds",p,flags,Set.of()));assertEquals(0,p.getCoins());flags.add("gather:FORAGING:item0:8");NpcQuests.command("npc_deliver","warm_beds",p,flags,Set.of());assertEquals(30,p.getCoins());assertEquals(0,Gathering.FORAGING.owned(flags,0));assertThrows(IllegalArgumentException.class,()->NpcQuests.command("npc_deliver","warm_beds",p,flags,Set.of()));assertEquals(true,NpcQuests.view(flags,Set.of()).get(0).get("complete"));}
}
