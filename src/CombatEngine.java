import java.util.*;

/** One encounter per instance. Caller owns input/output; accepted commands advance one round. */
public final class CombatEngine {
    private int round;
    public int getRound() { return round; }
    public Map<String,Integer> snapshotCooldowns() { return Map.copyOf(cooldowns); }
    public void restoreProgress(int savedRound, Map<String,Integer> savedCooldowns) {
        if(savedRound < 0 || savedCooldowns.values().stream().anyMatch(v -> v < 0)) throw new IllegalArgumentException();
        round=savedRound; cooldowns.clear(); cooldowns.putAll(savedCooldowns);
    }
    private final Player player; private final Enemy enemy; private final Random random;
    private final Map<String,Integer> cooldowns=new HashMap<>();
    private CombatResult.Outcome outcome=CombatResult.Outcome.ACTIVE;
    public CombatEngine(Player player, Enemy enemy, Random random) {
        this.player=Objects.requireNonNull(player);this.enemy=Objects.requireNonNull(enemy);this.random=Objects.requireNonNull(random);
        if(player.isDead()||enemy.isDead())throw new IllegalArgumentException("Combatants must be alive");
        player.clearStatuses(); enemy.clearStatuses();
    }
    public int getCooldown(String id){return cooldowns.getOrDefault(id,0);}
    public CombatResult.Outcome getOutcome(){return outcome;}
    public CombatResult performAction(CombatAction action){return performAction(action,null);}
    public CombatResult performAction(CombatAction action,String abilityId) {
        List<String> events=new ArrayList<>();
        if(outcome!=CombatResult.Outcome.ACTIVE)return result(false,List.of("Encounter is over."));
        if(action==null)return result(false,List.of("Choose an action."));
        Ability ability=null;
        boolean stunned=player.hasStatus(StatusEffect.Kind.STUN);
        if(!stunned && action==CombatAction.ABILITY) {
            ability=player.getAvailableAbilities().stream().filter(a->a.id().equals(abilityId)).findFirst().orElse(null);
            if(ability==null || getCooldown(ability.id())>0 || player.getResource()<ability.cost())
                return result(false,List.of("Ability unavailable, cooling down, or insufficient resource."));
        }
        if(!stunned && action==CombatAction.POTION && (player.getPotions()==0 || player.getHealth()==player.getMaxHealth()))
            return result(false,List.of("Potion cannot be used."));
        cooldowns.replaceAll((id,turns)->Math.max(0,turns-1));
        int healthBeforeAction=enemy.getHealth();
        if(stunned) events.add("You are stunned.");
        else switch(action) {
            case ATTACK -> events.add("Attack dealt "+enemy.receiveDamage(player.rollDamage(random,-2,2),DamageType.PHYSICAL)+" damage.");
            case ABILITY -> {player.spendResource(ability.cost());ability.effect().apply(player,enemy,random,events);cooldowns.put(ability.id(),ability.cooldown());}
            case DEFEND -> {player.applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));player.restoreResource(6);events.add("You brace for the next attack and recover 6 resource.");}
            case POTION -> {player.usePotion();events.add("You drink a potion.");}
            case FLEE -> {if(random.nextDouble()<player.getFleeChance()){outcome=CombatResult.Outcome.FLED;events.add("You escaped.");}else events.add("Escape failed.");}
        }
        if(!stunned)player.getWeaponAttribute().apply(action,healthBeforeAction-enemy.getHealth(),player,enemy,events);
        player.endTurn();
        if(player.isDead())outcome=CombatResult.Outcome.DEFEAT;
        if(outcome==CombatResult.Outcome.ACTIVE && !enemy.isDead()) {
            if(!enemy.hasStatus(StatusEffect.Kind.STUN)) {
                enemy.performTurn(round,player,random,events);
            }
            else events.add(enemy.getName()+" is stunned.");
            enemy.endTurn();
        }
        if(outcome==CombatResult.Outcome.ACTIVE) {
            if(player.isDead())outcome=CombatResult.Outcome.DEFEAT;
            else if(enemy.isDead()) {
                outcome=CombatResult.Outcome.VICTORY;
                int xp=enemy.rollXpReward(random), coins=enemy.rollCoinReward(random), oldLevel=player.getLevel();
                player.addCoins(coins);player.gainXp(xp);
                events.add("Victory! +"+xp+" XP, +"+coins+" coins.");
                if(player.getLevel()>oldLevel)events.add("Level up! Now level "+player.getLevel()+"; maximum health and resource increased.");
            }
        }
        round++;
        if(!player.isDead())player.restoreResource(4);
        if(outcome!=CombatResult.Outcome.ACTIVE){player.clearStatuses();enemy.clearStatuses();}
        return result(true,events);
    }
    private CombatResult result(boolean accepted,List<String> events){return new CombatResult(accepted,outcome,events,player.getHealth(),enemy.getHealth());}
}
