import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class RouteRewardTest {
    private GameSession finishing(GameSave s) {
        GameSave.BattleData b=new GameSave.BattleData("Timberline Packleader",5,100,1,1,0,true,0,Map.of(),List.of(),List.of());
        return GameSession.restore(new GameSave(1,s.player(),"COMBAT",s.area(),4,s.kills(),s.deaths(),0,0,s.cleared(),s.claimed(),s.inventory(),s.equipped(),s.log(),b),new Random(1));
    }
    @Test void firstClearBonusCannotRepeatAndEarlyLootStaysWithinRoute() {
        Player p=new Mage("Route");p.setLevel(10);p.setHealth(p.getMaxHealth());
        GameSession game=new GameSession(p,new Random(1));game.command("adventure","WHISPERING_WOODS:0");
        game=finishing(game.snapshot());game.command("combat","ATTACK");
        while(game.snapshot().mode().equals("OBJECTIVE"))game.command("objective","");
        assertEquals(152,game.snapshot().player().xp());assertTrue(game.snapshot().cleared().isEmpty());
        assertTrue(game.snapshot().inventory().stream().allMatch(i->i.level()<=5&&i.rarity()!=Equipment.Rarity.EPIC));
        game=finishing(game.snapshot());game.command("combat","ATTACK");
        while(game.snapshot().mode().equals("OBJECTIVE"))game.command("objective","");
        assertEquals(204,game.snapshot().player().xp());
        game.command("town","");game.command("adventure","WHISPERING_WOODS:1");
        assertEquals("Hollow Reach",((Map<?,?>)game.view().get("route")).get("name"));
    }
}
