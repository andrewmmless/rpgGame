import java.util.*;

public abstract class Player extends Character {
    private final PlayerClass playerClass;
    private int level=1, xp, coins, potions=3, swordDamage, resource=40;
    protected Player(String name, PlayerClass type) {
        super(name,type.health,type.attack,type.armour); playerClass=type;
    }
    public final PlayerClass getPlayerClass(){return playerClass;}
    public abstract List<Ability> getAbilities();
    public List<Ability> getAvailableAbilities(){return getAbilities().stream().filter(a->a.unlockLevel()<=level).toList();}
    public double getFleeChance(){return 0.65;}
    public int getResource(){return resource;} public int getMaxResource(){return 40+2*(level-1);}
    public void setResource(int amount){resource=Math.max(0,Math.min(getMaxResource(),amount));}
    public void restoreResource(int amount){if(amount<0)throw new IllegalArgumentException(); setResource(resource+amount);}
    public void spendResource(int amount){if(amount<0 || amount>resource)throw new IllegalArgumentException(); resource-=amount;}
    public void gainXp(int amount) {
        if(amount<0)throw new IllegalArgumentException("Negative XP");
        long total=(long)xp+amount;
        while(level<Balance.MAX_LEVEL && total>=Balance.xpNeeded(level)) {
            total-=Balance.xpNeeded(level); level++; recalculateStats(); health=maxHealth; resource=getMaxResource();
        }
        xp=level==Balance.MAX_LEVEL?0:(int)total;
    }
    private void recalculateStats(){maxHealth=playerClass.health+10*(level-1); attackPower=playerClass.attack+3*(level-1); defence=playerClass.armour+2*(level-1); health=Math.min(health,maxHealth);}
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
    @Override public int rollDamage(Random random,int min,int max){return super.rollDamage(random,min,max)+swordDamage;}
}
