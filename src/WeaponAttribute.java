import java.util.List;

/** Small, non-stacking weapon effects shared by all classes. */
public enum WeaponAttribute {
    NONE("No special attribute"),
    PIERCING("Basic attacks deal 2 additional damage that bypasses armour; guarding still reduces it."),
    SIPHON("Damaging abilities restore up to 2 health. Healing and resource abilities do not trigger it."),
    FOCUS("Defending restores 4 additional resource, up to your resource maximum.");
    private final String description;
    WeaponAttribute(String description){this.description=description;}
    public String description(){return description;}
    public void apply(CombatAction action,int dealt,Player player,Enemy enemy,List<String> events) {
        switch(this) {
            case PIERCING -> {if(action==CombatAction.ATTACK&&dealt>0&&!enemy.isDead())events.add("Piercing dealt "+enemy.receiveDamage(2,DamageType.TRUE)+" bonus damage.");}
            case SIPHON -> {if(action==CombatAction.ABILITY&&dealt>0){int before=player.getHealth();player.heal(2);events.add("Siphon restored "+(player.getHealth()-before)+" health.");}}
            case FOCUS -> {if(action==CombatAction.DEFEND){int before=player.getResource();player.restoreResource(4);events.add("Focus restored "+(player.getResource()-before)+" extra resource.");}}
            default -> { }
        }
    }
}
