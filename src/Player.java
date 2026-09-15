import java.util.*;

public abstract class Player extends Character {
    private final PlayerClass playerClass;
    private int weaponBonus, armourBonus;
    private Set<String> buildFlags=Set.of(),storyClears=Set.of();
    private WeaponAttribute weaponAttribute=WeaponAttribute.NONE;
    public WeaponAttribute getWeaponAttribute(){return weaponAttribute;}
    public void equipAttribute(WeaponAttribute attribute){weaponAttribute=Objects.requireNonNull(attribute); }
    private int level=1, xp, coins, potions=3, swordDamage, resource=40;
    protected Player(String name, PlayerClass type) {
        super(name,type.health,type.attack,type.armour); playerClass=type;
    }
    public final PlayerClass getPlayerClass(){return playerClass;}
    public abstract List<Ability> getAbilities();
    public void configureBuild(Set<String> flags,Set<String> cleared){buildFlags=Set.copyOf(flags);storyClears=Set.copyOf(cleared);recalculateStats();resource=Math.min(resource,getMaxResource());}
    public List<Ability> getAllBuildAbilities(){List<Ability> all=new ArrayList<>(getAbilities());all.addAll(BuildAbilities.forClass(playerClass));return all;}
    public boolean abilityUnlocked(Ability a){return a.id().startsWith("signature_")?CharacterBuild.specialisation(buildFlags)!=null:a.id().startsWith("trained_")?storyClears.contains(Area.STONEFANG_CAVES.name()):a.unlockLevel()<=level;}
    public List<Ability> getAvailableAbilities(){boolean custom=buildFlags.stream().anyMatch(f->f.startsWith("build:slot:"));return getAllBuildAbilities().stream().filter(this::abilityUnlocked).filter(a->custom?buildFlags.contains("build:slot:"+a.id()):!a.id().startsWith("signature_")&&!a.id().startsWith("trained_")).map(this::trainedAbility).toList();}
    public Ability trainedAbility(Ability base){int rank=CharacterBuild.value(buildFlags,"skill:"+base.id());return new Ability(base.id(),base.name(),base.cost()==0?0:Math.max(1,base.cost()-rank),base.cooldown(),base.unlockLevel(),(u,t,r,e)->{int targetHealth=t.getHealth(),hp=u.getHealth(),resourceBefore=u.getResource();base.effect().apply(u,t,r,e);CharacterBuild.Specialisation spec=CharacterBuild.specialisation(buildFlags);if(u.getResource()>resourceBefore&&rank>0){u.restoreResource(rank*2);e.add("Training improves resource recovery.");}int damage=Math.max(0,targetHealth-t.getHealth()),heal=Math.max(0,u.getHealth()-hp);int damageBonus=rank*8+(spec==null?0:spec.damage);int healBonus=rank*8+(spec==CharacterBuild.Specialisation.LIGHTWARDEN?20:0);if(damage>0&&damageBonus>0&&!t.isDead())e.add("Build bonus: "+t.receiveDamage(damage*damageBonus/100,DamageType.TRUE)+" extra damage.");if(heal>0&&healBonus>0){int before=u.getHealth();u.heal(heal*healBonus/100);e.add("Build bonus: "+(u.getHealth()-before)+" extra healing.");}});}

    public double getFleeChance(){return 0.65;}
    public int getResource(){return resource;} public int getMaxResource(){return 40+2*(level-1)+2*CharacterBuild.value(buildFlags,"FOCUS")+(CharacterBuild.specialisation(buildFlags)==null?0:CharacterBuild.specialisation(buildFlags).resource);}
    public void setResource(int amount){resource=Math.max(0,Math.min(getMaxResource(),amount));}
    public void restoreResource(int amount){if(amount<0)throw new IllegalArgumentException(); setResource(resource+amount);}
    public void spendResource(int amount){if(amount<0 || amount>resource)throw new IllegalArgumentException(); resource-=amount;}
    public void gainXp(int amount) {
        if(amount<0)throw new IllegalArgumentException("Negative XP");
        long total=(long)xp+amount;
        while(level<Balance.MAX_LEVEL && total>=Balance.xpNeeded(level)) {
            int oldMaxHealth=maxHealth,oldMaxResource=getMaxResource();
            total-=Balance.xpNeeded(level); level++; recalculateStats();
            if(health>0)heal(maxHealth-oldMaxHealth);
            restoreResource(getMaxResource()-oldMaxResource);
        }
        xp=level==Balance.MAX_LEVEL?0:(int)total;
    }
    private void recalculateStats(){CharacterBuild.Specialisation spec=CharacterBuild.specialisation(buildFlags);maxHealth=(playerClass.health+10*(level-1)+4*CharacterBuild.value(buildFlags,"VITALITY"))*(100+(spec==null?0:spec.health))/100; attackPower=playerClass.attack+3*(level-1)+CharacterBuild.value(buildFlags,"POWER"); defence=(playerClass.armour+2*(level-1)+CharacterBuild.value(buildFlags,"ARMOUR"))*(100+(spec==null?0:spec.armour))/100; health=Math.min(health,maxHealth);}
    public int getLevel(){return level;} public int getXp(){return xp;}
    public int getXpToNextLevel(){return level==Balance.MAX_LEVEL?0:Balance.xpNeeded(level);}
    public void setLevel(int value){if(value<1||value>Balance.MAX_LEVEL)throw new IllegalArgumentException(); level=value; recalculateStats(); resource=Math.min(resource,getMaxResource());}
    public void setXp(int value){if(value<0 || (level<Balance.MAX_LEVEL && value>=getXpToNextLevel()))throw new IllegalArgumentException(); xp=level==Balance.MAX_LEVEL?0:value;}
    public int getCoins(){return coins;} public void setCoins(int value){if(value<0)throw new IllegalArgumentException(); coins=value;}
    public void addCoins(int amount){if(amount<0)throw new IllegalArgumentException(); coins=Math.addExact(coins,amount);}
    public boolean spendCoins(int amount){if(amount<0)throw new IllegalArgumentException(); if(amount>coins)return false; coins-=amount;return true;}
    public int getPotions(){return potions;} public void setPotions(int value){if(value<0)throw new IllegalArgumentException();potions=value;}
    public void addPotion(){potions++;}
    public boolean usePotion(){if(potions==0 || health==maxHealth || isDead())return false;potions--;heal(Math.max(1,maxHealth*40/100));return true;}
    public int getSwordDamage(){return swordDamage;} public void setSwordDamage(int value){if(value<0)throw new IllegalArgumentException(); swordDamage=value;}
    public void upgradeSword(int amount){if(amount<0)throw new IllegalArgumentException();swordDamage=Math.addExact(swordDamage,amount);}
    public void setHealth(int value){health=Math.max(0,Math.min(maxHealth,value));}
    public void equipBonuses(int weapon, int armour) {
        if (weapon < 0 || armour < 0) throw new IllegalArgumentException("Invalid equipment");
        weaponBonus = weapon; armourBonus = armour;
    }
    @Override public int getAttackPower() { return super.getAttackPower() + weaponBonus + swordDamage; }
    @Override public int getDefence() { return super.getDefence() + armourBonus; }
    @Override public int rollDamage(Random random,int min,int max){return super.rollDamage(random,min,max)+swordDamage+weaponBonus;}
}
