import java.util.*;
/** Console adapter only; all combat rules live in CombatEngine. */
public final class Fight {
    public static void start(Scanner input, Random random, Player player, Enemy enemy) {
        CombatEngine engine=new CombatEngine(player,enemy,random);
        System.out.println("Encountered Lv."+enemy.getLevel()+" "+enemy.getName());
        while(engine.getOutcome()==CombatResult.Outcome.ACTIVE && input.hasNextLine()) {
            System.out.println("HP "+player.getHealth()+"/"+player.getMaxHealth()+" | Resource "+player.getResource()+"/"+player.getMaxResource()+" | Enemy HP "+enemy.getHealth());
            System.out.println("attack, special, defend, heal, run");
            for(Ability a:player.getAvailableAbilities())System.out.println(a.id()+": "+a.name()+" (cost "+a.cost()+", cooldown "+engine.getCooldown(a.id())+")");
            String choice=input.nextLine().trim().toLowerCase(Locale.ROOT);
            CombatAction action=switch(choice){case "attack"->CombatAction.ATTACK;case "defend"->CombatAction.DEFEND;case "heal"->CombatAction.POTION;case "run"->CombatAction.FLEE;default->CombatAction.ABILITY;};
            String id=choice.equals("special")?player.getAvailableAbilities().get(0).id():choice;
            engine.performAction(action,id).events().forEach(System.out::println);
        }
    }
}
