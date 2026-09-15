/** Encounter stats and reward ranges, independent of presentation. */
public class Enemy extends Character {

    private boolean boss;
    public Enemy asBoss() { boss = true; return this; }
    public boolean isBoss() { return boss; }
    public void restoreHealth(int value) { if(value < 0 || value > maxHealth) throw new IllegalArgumentException(); health=value; }
    public enum Move { ATTACK, CHARGE, HEAVY, GUARD, POISON, DRAIN, SAP, RECOVER }
    public Move move(int round) {
        if(boss) return round%3==0?Move.CHARGE:round%3==1?Move.HEAVY:Move.ATTACK;
        if(level<3) return Move.ATTACK;
        return switch(name) {
            case "Frost Elemental" -> round%3==0?Move.SAP:Move.ATTACK;
            case "Dark Cultist" -> round%3==0?Move.RECOVER:Move.ATTACK;
            case "Cave Spider", "Storm Harpy" -> round%3==0?Move.POISON:Move.ATTACK;
            case "Stone Golem", "Skeleton Knight", "Ancient Guardian" -> round%3==0?Move.GUARD:Move.ATTACK;
            case "Wraith", "Shadow Knight" -> round%3==1?Move.DRAIN:Move.ATTACK;
            case "Orc Grunt", "Cave Troll", "Wyvern", "Tower Shade" -> round%3==0?Move.CHARGE:round%3==1?Move.HEAVY:Move.ATTACK;
            default -> Move.ATTACK;
        };
    }
    public String intent(int round) {
        return switch(move(round)) {
            case SAP -> "Chilling siphon — defend to protect your resource";
            case RECOVER -> "Restoring vitality — stun to interrupt";
            case ATTACK -> "Attack";
            case CHARGE -> "Gathering strength";
            case HEAVY -> "Heavy strike — defend!";
            case GUARD -> "Guard — your next attack will be weakened";
            case POISON -> "Poison strike — stun to interrupt";
            case DRAIN -> "Life drain — damage also heals the enemy";
        };
    }
    public String advice(int round){return switch(move(round)){
        case CHARGE -> "An opening: attack, heal or work on the mission before the heavy strike.";
        case HEAVY -> "Defend now to halve damage, or interrupt with a stun.";
        case GUARD -> "Save your strongest attack until the guard fades. Recover or work on the objective.";
        case POISON -> "A stun stops the poison strike before it lands.";
        case DRAIN -> "Defending reduces damage and the health this enemy steals.";
        case SAP -> "Defend to block the resource drain. Ward disruption also provides guard.";
        case RECOVER -> "Stun to prevent healing, or use the opening for your own recovery.";
        default -> "Balance damage with your health, resource and mission objective.";
    };}
    public double attackMultiplier(int round) { return move(round)==Move.HEAVY?2.6:move(round)==Move.CHARGE?0:1; }
    public void performTurn(int round, Player target, java.util.Random random, java.util.List<String> events) {
        Move next=move(round);
        if(next==Move.CHARGE) {events.add(name+" gathers strength. A heavy strike is coming!");return;}
        if(next==Move.GUARD) {
            applyStatus(new StatusEffect("guard",StatusEffect.Kind.GUARD,0,2));
            events.add(name+" guards. Use this opening to recover or prepare.");return;
        }
        if(next==Move.RECOVER){int before=health;heal(Math.max(4,maxHealth/8));events.add(name+" recovered "+(health-before)+" health.");return;}
        if(next==Move.SAP){if(target.hasStatus(StatusEffect.Kind.GUARD))events.add("Your guard blocks the resource siphon.");else{int lost=Math.min(8,target.getResource());target.spendResource(lost);events.add(name+" drained "+lost+" resource.");}}
        int dealt=target.receiveDamage((int)Math.round(rollDamage(random,-1,1)*attackMultiplier(round)),DamageType.PHYSICAL);
        events.add(name+" dealt "+dealt+" damage.");
        if(next==Move.POISON && !target.isDead()) {
            target.applyStatus(new StatusEffect("poison",StatusEffect.Kind.DAMAGE_OVER_TIME,Math.max(2,level/2),2));events.add("Poison will hurt for two turns.");
        }
        if(next==Move.DRAIN) {heal(dealt/2);events.add(name+" drained "+dealt/2+" health.");}
    }
    private int level;
    private int coinRewardMin;
    private int coinRewardMax;
    private int xpRewardMin;
    private int xpRewardMax;

    public Enemy(String name, int level, int health, int attackPower, int defence,
                 int coinRewardMin, int coinRewardMax,
                 int xpRewardMin, int xpRewardMax) {
        super(name, health, attackPower, defence);
        if(level<1 || level>Balance.MAX_LEVEL || coinRewardMin<0 || coinRewardMax<coinRewardMin || xpRewardMin<0 || xpRewardMax<xpRewardMin)
            throw new IllegalArgumentException("Invalid enemy rewards or level");
        this.level = level;
        this.coinRewardMin = coinRewardMin;
        this.coinRewardMax = coinRewardMax;
        this.xpRewardMin = xpRewardMin;
        this.xpRewardMax = xpRewardMax;
    }

    public int rollCoinReward(java.util.Random gen) {
        return gen.nextInt(coinRewardMax - coinRewardMin + 1) + coinRewardMin;
    }

    public int rollXpReward(java.util.Random gen) {
        return gen.nextInt(xpRewardMax - xpRewardMin + 1) + xpRewardMin;
    }

    public int getLevel() { return level; }
}
