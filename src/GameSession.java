import java.util.*;

/** UI-independent campaign state machine, shared by the browser and future adapters. */
public final class GameSession {
    public enum Mode { TOWN, TRAIL, COMBAT, SHRINE, COMPLETE, DEFEAT }
    private final Player player;
    private final Random random;
    private Mode mode=Mode.TOWN;
    private Area area=Area.WHISPERING_WOODS;
    private int room, kills, deaths, towerBest, towerFloor;
    private final Set<String> cleared=new HashSet<>(), claimed=new HashSet<>();
    private final List<Equipment> inventory=new ArrayList<>();
    private final EnumMap<Equipment.Slot,String> equipped=new EnumMap<>(Equipment.Slot.class);
    private final List<String> log=new ArrayList<>();
    private Enemy enemy;
    private CombatEngine combat;

    public GameSession(Player player, Random random) {
        this.player=Objects.requireNonNull(player); this.random=Objects.requireNonNull(random);
        say("Welcome to Cindergard, "+player.getName()+". Choose Whispering Woods to begin your first expedition.");
    }
    private void say(String text) { log.add(text); while(log.size()>40)log.remove(0); }
    private void require(boolean condition,String message) { if(!condition)throw new IllegalArgumentException(message); }
    private void inTown() { require(mode==Mode.TOWN,"Return to town first."); }
    private int activeRoute() {return claimed.stream().filter(f->f.startsWith("route:active:")).mapToInt(f->Integer.parseInt(f.substring(13))).findFirst().orElse(-1);}
    private SubArea route() {int index=activeRoute();return index<0||towerFloor>0?null:SubArea.get(index);}
    public boolean unlocked(Area target) {return SubArea.get(target.ordinal()*3).unlocked(claimed,cleared);}
    private SubArea highestRoute() {return SubArea.ALL.stream().filter(r->r.unlocked(claimed,cleared)).reduce((a,b)->b).orElse(SubArea.get(0));}
    private int shopLevel() {return Math.min(player.getLevel(),highestRoute().maxLevel());}
    private Equipment routeDrop(boolean boss,SubArea route) {
        int level=Math.max(route.minLevel(),Math.min(route.maxLevel(),player.getLevel()));
        return Equipment.regionalDrop(random,level,boss,player.getPlayerClass(),route.area());
    }
    public void command(String action,String value) {
        require(action!=null,"Choose an action.");
        if (value == null) value = "";
        switch(action) {
            case "story" -> {inTown();say(StoryPath.speak(value,claimed,cleared));}
            case "adventure" -> {
                inTown();String[] choice=value.split(":",-1);Area target=Area.valueOf(choice[0]);
                int index=choice.length==1?SubArea.ALL.stream().filter(r->r.area()==target&&r.unlocked(claimed,cleared)).filter(r->!r.complete(claimed,cleared)).mapToInt(SubArea::index).findFirst().orElse(target.ordinal()*3):Integer.parseInt(choice[1]);
                require(index>=0&&index<SubArea.ALL.size(),"Choose a route.");SubArea path=SubArea.get(index);
                require(path.area()==target&&path.unlocked(claimed,cleared),"Complete the preceding story route first.");
                require(!player.isDead(),"Rest before departing.");
                claimed.removeIf(f->f.startsWith("route:active:"));claimed.add("route:active:"+index);
                area=target;room=0;towerFloor=0;mode=Mode.TRAIL;
                say(path.complete(claimed,cleared)?"Patrol: "+path.name()+". Keep the route safe and gather equipment.":path.captain()+": "+path.opening());
            }
            case "tower" -> {
                inTown(); require(cleared.size()==4,"Defeat Victoria to unlock the Endless Tower.");
                towerFloor=towerBest+1;area=Area.DRAGONS_SPIRE;room=0;mode=Mode.TRAIL;
                say("You enter floor "+towerFloor+" of the Endless Tower.");
            }
            case "continue" -> {
                require(mode==Mode.TRAIL,"You cannot advance right now.");
                if(room==2) { mode=Mode.SHRINE; say("A spring bubbles beside an abandoned supply cache. Choose one before moving on."); }
                else {
                    enemy=route()==null?Campaign.encounter(area,player.getLevel(),room==4,random,towerFloor):route().encounter(room,player.getLevel(),random);
                    combat=new CombatEngine(player,enemy,random);mode=Mode.COMBAT;
                    say("Encountered "+enemy.getName()+" (level "+enemy.getLevel()+").");
                }
            }
            case "combat" -> fight(value);
            case "spring" -> {
                require(mode==Mode.SHRINE,"There is no spring here.");
                player.heal(player.getMaxHealth()*45/100);player.restoreResource(20);room++;mode=Mode.TRAIL;
                say("The spring restores 45% of your maximum health and 20 resource.");
            }
            case "cache" -> {
                require(mode==Mode.SHRINE,"There is no cache here.");
                player.addCoins(12+player.getLevel()*2);player.addPotion();room++;mode=Mode.TRAIL;
                say("You take a potion and a pouch of coins from the cache.");
            }
            case "town" -> {
                require(mode!=Mode.COMBAT,"Use Run to escape combat first.");
                require(mode!=Mode.DEFEAT,"Recover before returning.");
                mode=Mode.TOWN;enemy=null;combat=null;towerFloor=0;say("Returned to Hearthglen. Rest at the inn before your next expedition.");
            }
            case "rest" -> {
                inTown();player.setHealth(player.getMaxHealth());player.setResource(player.getMaxResource());
                say("A warm meal and a good night's rest. Health and resource fully restored, free of charge.");
            }
            case "recover" -> {
                require(mode==Mode.DEFEAT,"You do not need rescuing.");
                player.setHealth(player.getMaxHealth());player.setResource(player.getMaxResource());mode=Mode.TOWN;towerFloor=0;
                say("The wardens bring you home. Your equipment and progress are safe.");
            }
            case "potion" -> { inTown();require(player.usePotion(),"No potion needed or available.");say("Used a potion."); }
            case "buy_gear" -> {
                inTown();Equipment.Slot slot=Equipment.Slot.valueOf(value);
                require(inventory.size()<30,"Make room in your bag first.");
                int gearLevel=shopLevel();int price=20+gearLevel*6;require(player.spendCoins(price),"Not enough coins for this equipment.");
                Equipment bought=new Equipment(UUID.randomUUID().toString(),slot==Equipment.Slot.WEAPON?Equipment.weaponName(player.getPlayerClass(),Equipment.Rarity.COMMON):"Traveler's Coat",slot,Equipment.Rarity.COMMON,gearLevel,2+gearLevel/2,0);
                inventory.add(bought);say("Bought "+bought.name()+" for "+price+" coins. Equip it in your backpack.");
            }
            case "buy_potion" -> {
                inTown();require(player.getPotions()<99,"Your potion pouch is full.");
                require(player.spendCoins(8),"You need 8 coins.");player.addPotion();say("Bought a health potion for 8 coins.");
            }
            case "train" -> {
                inTown();require(!claimed.contains("training"),"You have completed the introductory training.");
                claimed.add("training");player.gainXp(15);say("Training complete: +15 XP. Defend halves damage; bosses warn you before heavy attacks.");
            }
            case "open_chest" -> {
                inTown();require(availableChests()>0,"Earn a chest by winning three fights, clearing a new region, or clearing a new tower floor.");
                require(inventory.size()<30,"Make room in your bag before opening a chest.");
                Equipment reward=routeDrop(true,highestRoute());
                inventory.add(reward);claimed.add("chest:"+openedChests());
                say("Opened a milestone chest: "+reward.name()+" ("+reward.rarity().name().toLowerCase(Locale.ROOT)+").");
            }
            case "equip" -> {
                inTown();Equipment item=item(value);equipped.put(item.slot(),item.id());applyEquipment();say("Equipped "+item.name()+".");
            }
            case "protect_item" -> {inTown();Equipment item=item(value);String key="keep:"+item.id();if(!claimed.remove(key))claimed.add(key);say("Updated item sale protection.");}
            case "sell_many" -> {
                inTown();String[] ids=value.split(",",-1);require(ids.length>=1&&ids.length<=30,"Choose 1–30 items.");
                Set<String> unique=new HashSet<>(Arrays.asList(ids));require(unique.size()==ids.length,"An item was selected more than once.");
                List<Equipment> items=unique.stream().map(this::item).toList();
                require(items.stream().noneMatch(i->equipped.containsValue(i.id())||claimed.contains("keep:"+i.id())),"Equipped or protected items cannot be sold.");
                int total=items.stream().mapToInt(Equipment::value).sum();player.addCoins(total);inventory.removeAll(items);
                say("Sold "+items.size()+" items for "+total+" coins.");
            }
            case "sell" -> {
                inTown();Equipment item=item(value);require(!claimed.contains("keep:"+item.id()),"Unprotect this item before selling it.");require(!equipped.containsValue(item.id()),"Unequip this item by equipping another before selling it.");
                inventory.remove(item);player.addCoins(item.value());say("Sold "+item.name()+" for "+item.value()+" coins.");
            }
            case "upgrade" -> {
                inTown();Equipment item=item(value);require(item.upgrades()<5,"This item is fully upgraded.");
                require(player.spendCoins(item.upgradeCost()),"Not enough coins for this upgrade.");
                inventory.set(inventory.indexOf(item),item.upgrade());applyEquipment();say("Upgraded "+item.name()+". Power increased by 2.");
            }
            case "claim" -> claim(value);
            default -> throw new IllegalArgumentException("Unknown action.");
        }
    }
    private void fight(String value) {
        require(mode==Mode.COMBAT,"There is no active battle.");
        CombatAction action;
        String id=null;
        if(value!=null && value.startsWith("ability:")) {action=CombatAction.ABILITY;id=value.substring(8);}
        else action=CombatAction.valueOf(Objects.requireNonNull(value));
        CombatResult result=combat.performAction(action,id);
        require(result.accepted(),String.join(" ",result.events()));
        result.events().forEach(this::say);
        switch(result.outcome()) {
            case VICTORY -> {
                kills++;drop(enemy.isBoss());
                if(room==4) {
                    if(towerFloor>0) {towerBest=Math.max(towerBest,towerFloor);say("Tower floor "+towerFloor+" cleared. A harder floor awaits.");}
                    else if(route()!=null) {
                        SubArea path=route();boolean first=!path.complete(claimed,cleared);
                        claimed.add(path.key());
                        if(first){int xp=Balance.xpNeeded(path.minLevel())*2;player.gainXp(xp);say(path.ending());say("First route clear: +"+xp+" XP."+(path.index()<11?" The next route is open.":" Your campaign is complete."));}
                        else say("Patrol complete. The road remains safe.");
                        if(path.index()%3==2){cleared.add(area.name());say(Campaign.region(area).quest()+" complete. Claim the region reward in your journal.");}
                    }
                    else {cleared.add(area.name());say("Region complete. Your existing expedition progress has been preserved.");}
                    mode=Mode.COMPLETE;room=5;
                } else { room++;mode=Mode.TRAIL; }
                combat=null;
            }
            case DEFEAT -> {
                deaths++;int lost=player.getCoins()/10;player.spendCoins(lost);
                mode=Mode.DEFEAT;combat=null;say("You fell in battle and lost "+lost+" coins. The wardens can rescue you; your save is safe.");
            }
            case FLED -> { mode=Mode.TOWN;combat=null;towerFloor=0;say("The expedition ends. Rest at the inn before trying again."); }
            default -> { }
        }
    }
    private int openedChests() { return (int)claimed.stream().filter(id->id.startsWith("chest:")).count(); }
    private int availableChests() { return Math.max(0,(kills>=3?1:0)+cleared.size()+towerBest-openedChests()); }
    private Equipment item(String id) { return inventory.stream().filter(i->i.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Item not found.")); }
    private void drop(boolean boss) {
        if(!boss && random.nextInt(100)>=55)return;
        Equipment item=towerFloor>0
            ? Equipment.drop(random,Math.min(player.getLevel(),enemy.getLevel()),boss,player.getPlayerClass())
            : route()!=null?routeDrop(boss,route()):Equipment.regionalDrop(random,player.getLevel(),boss,player.getPlayerClass(),area);
        if(inventory.size()>=30) {player.addCoins(item.value());say("Pack full: sold "+item.name()+" for "+item.value()+" coins.");}
        else {inventory.add(item);say("Found "+item.rarity().name().toLowerCase(Locale.ROOT)+" "+item.name()+". Equip it when you return to town.");}
    }
    private void applyEquipment() {
        int weapon=0,armour=0;player.equipAttribute(WeaponAttribute.NONE);
        for(Equipment item:inventory)if(item.id().equals(equipped.get(item.slot()))) {
            if(item.slot()==Equipment.Slot.WEAPON){weapon+=item.power();player.equipAttribute(item.attribute());}else armour+=item.power();
        }
        player.equipBonuses(weapon,armour);
    }
    private void claim(String id) {
        require(mode!=Mode.COMBAT,"Finish combat before claiming rewards.");
        require(!claimed.contains(id),"This reward was already claimed.");
        int xp,coins;
        if("first_steps".equals(id)) {require(kills>=3,"Defeat three enemies first.");xp=20;coins=20;}
        else {
            Area target=Area.valueOf(id);require(cleared.contains(id),"Defeat this region's boss first.");
            xp=Balance.xpNeeded(target.getMinLevel());coins=40+target.ordinal()*40;
        }
        claimed.add(id);player.gainXp(xp);player.addCoins(coins);say("Quest reward: +"+xp+" XP and +"+coins+" coins.");
    }
    public void returnFromCoop(int health,int resource,int potionsUsed,int xp,int coins,Equipment reward,boolean won) {
        inTown();player.setHealth(Math.max(1,health));player.setResource(resource);player.setPotions(Math.max(0,player.getPotions()-potionsUsed));
        if(won){player.gainXp(xp);player.addCoins(coins);kills++;if(inventory.size()<30)inventory.add(reward);else{player.addCoins(reward.value());say("Co-op reward sold because your bag was full.");}say("Co-op dungeon cleared: +"+xp+" XP, +"+coins+" coins and "+reward.name()+".");}
        else say("Returned from the co-op dungeon. Rest before another expedition.");
    }
    public GameSave snapshot() {
        GameSave.PlayerData pd=new GameSave.PlayerData(player.getName(),player.getPlayerClass(),player.getLevel(),player.getXp(),player.getHealth(),player.getResource(),player.getCoins(),player.getPotions(),player.getSwordDamage());
        GameSave.BattleData battle=mode==Mode.COMBAT?new GameSave.BattleData(enemy.getName(),enemy.getLevel(),enemy.getMaxHealth(),enemy.getHealth(),enemy.getAttackPower(),enemy.getDefence(),enemy.isBoss(),combat.getRound(),combat.snapshotCooldowns(),player.snapshotStatuses(),enemy.snapshotStatuses()):null;
        return new GameSave(1,pd,mode.name(),area,room,kills,deaths,towerBest,towerFloor,Set.copyOf(cleared),Set.copyOf(claimed),List.copyOf(inventory),Map.copyOf(equipped),List.copyOf(log),battle);
    }
    public static GameSession restore(GameSave save, Random random) {
        if(save.schemaVersion()!=1)throw new IllegalArgumentException("Unsupported save version.");
        GameSave.PlayerData pd=save.player();Player p=pd.type().create(pd.name());
        p.setLevel(pd.level());p.setXp(pd.xp());p.setHealth(pd.health());p.setResource(pd.resource());p.setCoins(pd.coins());p.setPotions(pd.potions());p.setSwordDamage(pd.swordDamage());
        GameSession game=new GameSession(p,random);
        game.mode=Mode.valueOf(save.mode());game.area=save.area();game.room=save.room();game.kills=save.kills();game.deaths=save.deaths();
        game.towerBest=save.towerBest();game.towerFloor=save.towerFloor();game.cleared.addAll(save.cleared());game.claimed.addAll(save.claimed());
        game.inventory.addAll(save.inventory());game.equipped.putAll(save.equipped());game.applyEquipment();game.log.clear();game.log.addAll(save.log());
        if(game.mode==Mode.COMBAT) {
            GameSave.BattleData b=Objects.requireNonNull(save.battle());int xp=Balance.enemyXp(b.level())*(b.boss()?2:1);
            game.enemy=new Enemy(b.name(),b.level(),b.maxHealth(),b.attack(),b.defence(),4+b.level(),8+b.level()*2,xp,xp);
            if(b.boss())game.enemy.asBoss();game.enemy.restoreHealth(b.health());game.combat=new CombatEngine(p,game.enemy,random);
            game.combat.restoreProgress(b.round(),b.cooldowns());p.restoreStatuses(b.playerStatuses());game.enemy.restoreStatuses(b.enemyStatuses());
        }
        return game;
    }
    public Map<String,Object> view() {
        Map<String,Object> view=new LinkedHashMap<>();
        view.put("player",Map.ofEntries(Map.entry("name",player.getName()),Map.entry("type",player.getPlayerClass()),Map.entry("rank",WorldNames.rank(player.getPlayerClass(),cleared)),Map.entry("title",cleared.contains(Area.DRAGONS_SPIRE.name())?"Dragonbane":""),Map.entry("level",player.getLevel()),Map.entry("xp",player.getXp()),Map.entry("nextXp",player.getXpToNextLevel()),Map.entry("health",player.getHealth()),Map.entry("maxHealth",player.getMaxHealth()),Map.entry("resource",player.getResource()),Map.entry("maxResource",player.getMaxResource()),Map.entry("coins",player.getCoins()),Map.entry("potions",player.getPotions()),Map.entry("attack",player.getAttackPower()),Map.entry("defence",player.getDefence()),Map.entry("statuses",player.getStatuses())));
        view.put("story",StoryPath.view(claimed,cleared));
        view.put("routes",SubArea.ALL.stream().map(r->Map.of("index",r.index(),"area",r.area(),"name",r.name(),"minLevel",r.minLevel(),"maxLevel",r.maxLevel(),"mission",r.mission(),"unlocked",r.unlocked(claimed,cleared),"complete",r.complete(claimed,cleared))).toList());
        if(route()!=null)view.put("route",Map.of("name",route().name(),"mission",route().mission(),"opening",route().opening(),"ending",route().ending(),"guardian",route().guardian()));
        view.put("storyArchive",SubArea.ALL.stream().filter(r->r.complete(claimed,cleared)).map(r->Map.of("name",r.name(),"text",r.ending())).toList());
        view.put("mode",mode);view.put("area",area);view.put("room",room);view.put("towerFloor",towerFloor);view.put("towerBest",towerBest);
        view.put("kills",kills);view.put("deaths",deaths);view.put("log",List.copyOf(log));view.put("cleared",Set.copyOf(cleared));view.put("claimed",Set.copyOf(claimed));
        view.put("chests",availableChests());view.put("chestEpicChance",Math.max(highestRoute().minLevel(),Math.min(highestRoute().maxLevel(),player.getLevel()))>=31?8:0);view.put("shopPrice",20+shopLevel()*6);view.put("shopLevel",shopLevel());
        view.put("inventory",inventory.stream().map(i->Map.of("item",i,"equipped",i.id().equals(equipped.get(i.slot())),"value",i.value(),"upgradeCost",i.upgradeCost(),"attributeDescription",i.attribute().description(),"protected",claimed.contains("keep:"+i.id()))).toList());
        view.put("regions",Campaign.REGIONS.stream().map(r->Map.of("id",r.area(),"name",r.area().getDisplayName(),"subtitle",r.subtitle(),"story",r.story(),"boss",r.boss(),"quest",r.quest(),"minLevel",r.area().getMinLevel(),"maxLevel",r.area().getMaxLevel(),"unlocked",unlocked(r.area()))).toList());
        view.put("abilities",player.getAbilities().stream().map(a->Map.of("id",a.id(),"name",a.name(),"cost",a.cost(),"unlockLevel",a.unlockLevel(),"cooldown",combat==null?0:combat.getCooldown(a.id()),"baseCooldown",a.cooldown())).toList());
        if(mode==Mode.COMBAT)view.put("enemy",Map.of("name",enemy.getName(),"level",enemy.getLevel(),"health",enemy.getHealth(),"maxHealth",enemy.getMaxHealth(),"boss",enemy.isBoss(),"intent",enemy.intent(combat.getRound()),"statuses",enemy.getStatuses()));
        return view;
    }
}
