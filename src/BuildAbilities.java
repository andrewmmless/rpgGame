import java.util.*;
/** Promotion skills are selected into the same four-slot loadout as starter abilities. */
public final class BuildAbilities {
    public static List<Ability> forClass(PlayerClass type){return switch(type){
        case WARRIOR -> List.of(new Ability("signature_bulwark","Bulwark",16,4,1,(u,t,r,e)->{u.applyStatus(new StatusEffect("bulwark",StatusEffect.Kind.GUARD,0,3));u.heal(u.getMaxHealth()/10);e.add("Bulwark: guard for two enemy turns and restore 10% health.");}),Ability.strike("trained_sundering","Sundering Blow",22,3,1.8,DamageType.TRUE,null));
        case MAGE -> List.of(Ability.strike("signature_cinder","Cinder Lance",18,3,1.9,DamageType.FIRE,null),new Ability("trained_barrier","Ember Barrier",14,4,1,(u,t,r,e)->{u.applyStatus(new StatusEffect("barrier",StatusEffect.Kind.GUARD,0,3));u.restoreResource(6);e.add("Ember Barrier: guard for two enemy turns, recover 6 resource.");}));
        case CLERIC -> List.of(new Ability("signature_sanctuary","Sanctuary",20,4,1,(u,t,r,e)->{u.heal(u.getMaxHealth()/4);u.applyStatus(new StatusEffect("sanctuary",StatusEffect.Kind.GUARD,0,2));e.add("Sanctuary: restore 25% health and guard the next attack.");}),Ability.strike("trained_radiance","Radiant Chains",20,4,1.5,DamageType.HOLY,StatusEffect.Kind.STUN));
        case ROGUE -> List.of(Ability.strike("signature_ambush","Ambush",18,3,2.0,DamageType.PHYSICAL,null),new Ability("trained_smoke","Smoke Step",12,4,1,(u,t,r,e)->{u.applyStatus(new StatusEffect("smoke",StatusEffect.Kind.GUARD,0,3));u.restoreResource(8);e.add("Smoke Step: guard for two enemy turns and recover 8 resource.");}));
    };}
}
