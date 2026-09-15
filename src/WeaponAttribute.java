import java.util.List;

/** Small, non-stacking weapon effects shared by all classes. */
public enum WeaponAttribute {
    NONE("No special attribute"),
    PIERCING("Basic attacks deal 2 additional damage that bypasses armour; guarding still reduces it."),
    SIPHON("Damaging abilities restore up to 2 health. Healing and resource abilities do not trigger it."),
    FOCUS("Defending restores 4 additional resource, up to your resource maximum."),
    CONDUIT("Damaging abilities restore 3 resource after their cost is paid."),
    VAMPIRIC("Basic attacks that deal damage restore 3 health."),
    CROWNWARD("Defending restores 6 resource and 4% maximum health; damaging abilities sacrifice up to 2 health (never fatal) for a 4 true-damage follow-up against surviving enemies.");
    private final String description;
    WeaponAttribute(String description){this.description=description;}
    public String description(){return description;}
    public void apply(CombatAction action,int dealt,Player player,Enemy enemy,List<String> events) {
        switch(this) {
            case CROWNWARD -> {if(action==CombatAction.DEFEND){player.restoreResource(6);player.heal(Math.max(1,player.getMaxHealth()/25));events.add("Crownward restores your strength.");}else if(action==CombatAction.ABILITY&&dealt>0){player.setHealth(Math.max(1,player.getHealth()-2));if(!enemy.isDead())enemy.receiveDamage(4,DamageType.TRUE);events.add("Crownfire trades 2 health for a 4-damage follow-up.");}}
            case CONDUIT -> {if(action==CombatAction.ABILITY&&dealt>0){player.restoreResource(3);events.add("Conduit restored resource.");}}
            case VAMPIRIC -> {if(action==CombatAction.ATTACK&&dealt>0){player.heal(3);events.add("Bloodsteel restored health.");}}
            case PIERCING -> {if(action==CombatAction.ATTACK&&dealt>0&&!enemy.isDead())events.add("Piercing dealt "+enemy.receiveDamage(2,DamageType.TRUE)+" bonus damage.");}
            case SIPHON -> {if(action==CombatAction.ABILITY&&dealt>0){int before=player.getHealth();player.heal(2);events.add("Siphon restored "+(player.getHealth()-before)+" health.");}}
            case FOCUS -> {if(action==CombatAction.DEFEND){int before=player.getResource();player.restoreResource(4);events.add("Focus restored "+(player.getResource()-before)+" extra resource.");}}
            default -> { }
        }
    }
}
