import java.util.*;

/** UI-independent campaign state machine, shared by the browser and future adapters. */
public final class GameSession {
    public enum Mode { TOWN, TRAIL, COMBAT, SHRINE, OBJECTIVE, COMPLETE, DEFEAT }
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
    private boolean rivalActive(){return claimed.stream().anyMatch(f->f.startsWith("rival:active:"));}
    private int rivalValue(String key){String prefix="rival:"+key+":";return claimed.stream().filter(f->f.startsWith(prefix)).mapToInt(f->Integer.parseInt(f.substring(prefix.length()))).findFirst().orElseThrow();}
    private boolean missionActive(){return !rivalActive()&&route()!=null&&claimed.contains("mission:enabled");}
    private int missionValue(String key,int fallback){String prefix="mission:"+key+":";return claimed.stream().filter(f->f.startsWith(prefix)).mapToInt(f->Integer.parseInt(f.substring(prefix.length()))).findFirst().orElse(fallback);}
    private void setMissionValue(String key,int value){String prefix="mission:"+key+":";claimed.removeIf(f->f.startsWith(prefix));claimed.add(prefix+value);}
    private void workObjective(){
        require(missionActive()&&(mode==Mode.COMBAT||mode==Mode.OBJECTIVE),"No mission objective here.");
        if(mode==Mode.COMBAT){fight("OBJECTIVE");return;}
        setMissionValue("work",Math.min(2,missionValue("work",0)+1));say("The company secures the site: "+missionValue("work",0)+"/2.");
        if(missionValue("work",0)==2)finishRoom();
    }
    private int totalXp(){int total=player.getXp();for(int level=1;level<player.getLevel();level++)total+=Balance.xpNeeded(level);return total;}
    private void rewardAdd(String key,int value){if(claimed.contains("mission:ledger")&&value>0)setMissionValue("reward_"+key,missionValue("reward_"+key,0)+value);}
    public void command(String action,String value) {
        int xpBefore=totalXp(),coinsBefore=player.getCoins();boolean rivalWasActive=rivalActive();
        require(action!=null,"Choose an action.");
        if (value == null) value = "";
        switch(action) {
            case "npc_accept","npc_deliver" -> {inTown();say(NpcQuests.command(action,value,player,claimed,cleared));}
            case "rival" -> {
                inTown();RivalEncounters.Duel duel=RivalEncounters.get(Integer.parseInt(value));require(SubArea.get(duel.route()).complete(claimed,cleared)&&(duel.id()==0||claimed.contains(RivalEncounters.key(duel.id()-1))),"Complete the required story route and previous duel first.");require(!claimed.contains(RivalEncounters.key(duel.id())),"This rival encounter is already complete.");require(!player.isDead(),"Rest before the duel.");
                claimed.add("rival:active:"+duel.id());claimed.add("rival:hp:"+player.getHealth());claimed.add("rival:resource:"+player.getResource());claimed.add("rival:potions:"+player.getPotions());
                enemy=RivalEncounters.enemy(duel);combat=new CombatEngine(player,enemy,random);mode=Mode.COMBAT;say(duel.opening());say("Practice duel: health, resource and potions are restored afterward. No death penalty; no story gate.");
            }
            case "upgrade_tool" -> {inTown();say(Gathering.valueOf(value).upgrade(player,claimed,cleared));}
            case "survey" -> {inTown();String[] parts=value.split(":");require(parts.length==2,"Choose a site.");say(Gathering.valueOf(parts[0]).gather(Integer.parseInt(parts[1]),player,claimed,cleared,random,true));}
            case "gather","sell_material" -> {inTown();String[] parts=value.split(":");require(parts.length==2,"Choose a gathering site.");Gathering profession=Gathering.valueOf(parts[0]);int tier=Integer.parseInt(parts[1]);say(action.equals("gather")?profession.gather(tier,player,claimed,cleared,random):profession.sell(tier,player,claimed));}
            case "attribute","specialise","upgrade_ability","loadout","reset_build" -> {inTown();CharacterBuild.command(action,value,player,claimed,cleared);say("Character build updated. Rest to fill any increased health or resource capacity.");}
            case "craft" -> {inTown();Equipment item=Crafting.craft(value,player,claimed,cleared,inventory.size());if(item!=null){inventory.add(item);if(item.rarity()==Equipment.Rarity.LEGENDARY)claimed.add("keep:"+item.id());}say("Crafted "+Crafting.recipe(value).name()+".");}
            case "objective" -> workObjective();
            case "story_catchup" -> {inTown();int xp=StoryConsequences.catchupXp(player,claimed,cleared);require(xp>0,"Your campaign training is already current.");player.gainXp(xp);say("Campaign training catches up your completed story: +"+xp+" XP.");}
            case "story_choice" -> {inTown();StoryConsequences.choose(value,claimed,cleared);say("The company records your decision: "+value+" the sealed order.");}
            case "story" -> {inTown();say(StoryPath.speak(value,claimed,cleared));}
            case "adventure" -> {
                inTown();String[] choice=value.split(":",-1);Area target=Area.valueOf(choice[0]);
                int index=choice.length==1?SubArea.ALL.stream().filter(r->r.area()==target&&r.unlocked(claimed,cleared)).filter(r->!r.complete(claimed,cleared)).mapToInt(SubArea::index).findFirst().orElse(target.ordinal()*3):Integer.parseInt(choice[1]);
                require(index>=0&&index<SubArea.ALL.size(),"Choose a route.");SubArea path=SubArea.get(index);
                require(path.area()==target&&path.unlocked(claimed,cleared),"Complete the preceding story route first.");
                require(!player.isDead(),"Rest before departing.");
                claimed.removeIf(f->f.startsWith("route:active:"));claimed.add("route:active:"+index);
                claimed.removeIf(f->f.startsWith("mission:"));claimed.add("mission:enabled");claimed.add("mission:ledger");setMissionValue("integrity",100);
                area=target;room=0;towerFloor=0;mode=Mode.TRAIL;
                say(path.complete(claimed,cleared)?"Patrol: "+path.name()+". Keep the route safe and gather equipment.":path.captain()+": "+path.opening());
            }
            case "tower" -> {
                inTown(); require(cleared.size()==4,"Defeat Ashwing to unlock the Endless Tower.");
                towerFloor=towerBest+1;area=Area.DRAGONS_SPIRE;room=0;mode=Mode.TRAIL;
                say("You enter floor "+towerFloor+" of the Endless Tower.");
            }
            case "continue" -> {
                require(mode==Mode.TRAIL,"You cannot advance right now.");
                if(room==2) { mode=Mode.SHRINE; say("A spring bubbles beside an abandoned supply cache. Choose one before moving on."); }
                else {
                    enemy=route()==null?Campaign.encounter(area,player.getLevel(),room==4,random,towerFloor):route().encounter(room,player.getLevel(),random);
                    combat=new CombatEngine(player,enemy,random);if(claimed.contains("mission:scout"))player.applyStatus(new StatusEffect("scouted",StatusEffect.Kind.GUARD,0,2));mode=Mode.COMBAT;if(missionActive())setMissionValue("work",0);
                    say("Encountered "+enemy.getName()+" (level "+enemy.getLevel()+").");
                }
            }
            case "combat" -> fight(value);
            case "road_choice" -> {
                require(mode==Mode.SHRINE&&route()!=null,"Choose at a story waystation.");
                require(Set.of("rescue","scout").contains(value),"Choose a mission approach.");
                if(value.equals("rescue")){require(player.getResource()>=12,"Rescuing survivors needs 12 resource.");player.spendResource(12);claimed.add("mission:rescue");say("You escort stranded workers to safety. Complete this route for a relief payment; you forgo the spring's recovery.");}
                else {claimed.add("mission:scout");say("You study the enemy approach. Start each remaining encounter guarded, but forgo the supply cache.");}
                room++;mode=Mode.TRAIL;
            }
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
                mode=Mode.TOWN;enemy=null;combat=null;towerFloor=0;say("Returned to the Crown District. Rest at the inn before your next expedition.");
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
        if(Set.of("combat","objective","cache","spring","road_choice").contains(action)&&!(action.equals("combat")&&value!=null&&rivalWasActive)){rewardAdd("xp",Math.max(0,totalXp()-xpBefore));rewardAdd("coins",Math.max(0,player.getCoins()-coinsBefore));}
        player.configureBuild(claimed,cleared);
    }
    private void fight(String value) {
        require(mode==Mode.COMBAT,"There is no active battle.");
        CombatAction action;
        String id=null;
        if(value!=null && value.startsWith("ability:")) {action=CombatAction.ABILITY;id=value.substring(8);}
        else action=CombatAction.valueOf(Objects.requireNonNull(value));
        boolean work=action==CombatAction.OBJECTIVE;
        require(!work||missionActive(),"No mission objective here.");
        boolean able=!player.hasStatus(StatusEffect.Kind.STUN);
        if(work&&able&&StoryConsequences.objective(route().index(),claimed)==MissionObjective.SEAL)player.applyStatus(new StatusEffect("ward_cover",StatusEffect.Kind.GUARD,0,2));
        CombatResult result=combat.performAction(action,id);
        require(result.accepted(),String.join(" ",result.events()));
        result.events().forEach(this::say);
        if(rivalActive()){
            if(result.outcome()!=CombatResult.Outcome.ACTIVE){int index=rivalValue("active"),hp=rivalValue("hp"),resource=rivalValue("resource"),potions=rivalValue("potions");boolean won=result.outcome()==CombatResult.Outcome.VICTORY;
                claimed.removeIf(f->f.startsWith("rival:active:")||f.startsWith("rival:hp:")||f.startsWith("rival:resource:")||f.startsWith("rival:potions:"));player.setHealth(hp);player.setResource(resource);player.setPotions(potions);mode=Mode.TOWN;enemy=null;combat=null;
                if(won){claimed.add(RivalEncounters.key(index));player.addCoins(20+index*20);say(RivalEncounters.get(index).victory());say("First duel victory: +"+(20+index*20)+" coins. Your equipment and story progress are unchanged.");}else say("Cedric offers a hand. ‘Again, when you're ready.’ Your health, resource and potions are restored; nothing was lost.");
            }return;
        }
        if(missionActive()&&result.outcome()!=CombatResult.Outcome.FLED){
            if(work&&able){setMissionValue("work",Math.min(2,missionValue("work",0)+1));say("Mission work "+missionValue("work",0)+"/2.");}
            if(StoryConsequences.objective(route().index(),claimed)==MissionObjective.ESCORT){int integrity=missionValue("integrity",100);setMissionValue("integrity",Math.max(0,Math.min(100,integrity+(work&&able?15:-5))));}
            if(missionValue("integrity",100)==0){if(player.isDead())applyDeathPenalty();mode=Mode.DEFEAT;combat=null;say("The convoy was lost. Your existing progress and gear are safe; regroup and try again.");return;}
        }
        switch(result.outcome()) {
            case VICTORY -> {
                kills++;drop(enemy.isBoss());
                if(missionActive()&&missionValue("work",0)<2){mode=Mode.OBJECTIVE;say("Enemy defeated. Secure the mission site before advancing.");}
                else finishRoom();
                combat=null;
            }
            case DEFEAT -> {
                applyDeathPenalty();mode=Mode.DEFEAT;combat=null;
            }
            case FLED -> { mode=Mode.TOWN;combat=null;towerFloor=0;say("The expedition ends. Rest at the inn before trying again."); }
            default -> { }
        }
    }
    private void applyDeathPenalty(){
        deaths++;int tier=highestRoute().area().ordinal(),percent=15+5*tier;
        int xpLoss=(player.getXp()*percent+99)/100,coinLoss=(int)(((long)player.getCoins()*(10+5*tier)+99)/100);
        player.setXp(player.getXp()-xpLoss);player.spendCoins(coinLoss);
        int materialsLost=0;for(Gathering g:Gathering.values())for(int i=0;i<3;i++){int owned=g.owned(claimed,i);int loss=owned*(5+5*tier)/100;if(loss>0){g.consume(claimed,i,loss);materialsLost+=loss;}}
        say("Defeat: lost "+xpLoss+" current-level XP, "+coinLoss+" coins and "+materialsLost+" gathered materials. Your level and equipment are preserved.");
    }
    private void finishRoom(){
                if(room==4) {
                    if(towerFloor>0) {towerBest=Math.max(towerBest,towerFloor);say("Tower floor "+towerFloor+" cleared. A harder floor awaits.");}
                    else if(route()!=null) {
                        SubArea path=route();boolean first=!path.complete(claimed,cleared);
                        claimed.add(path.key());
                        if(claimed.remove("mission:rescue")){int payment=20+path.minLevel()*2;player.addCoins(payment);claimed.add("story:relief:"+path.index());say("The rescued workers reach the capital: +"+payment+" coins. Your relief service is recorded.");}
                        if(first){int xp=Balance.storyClearXp(player.getLevel(),player.getXp(),path.index());player.gainXp(xp);say(StoryConsequences.ending(path.index(),path.ending(),claimed));say("First route clear: +"+xp+" XP."+(path.index()<11?" The next route is open.":" Your campaign is complete."));}
                        else say("Patrol complete. The road remains safe.");
                        if(path.index()%3==2){say("Boss trophy: +1 "+BossMaterials.award(claimed,path.index())+". Use it at the Forge.");cleared.add(area.name());say(Campaign.region(area).quest()+" complete. Claim the region reward in your journal.");}
                    }
                    else {cleared.add(area.name());say("Region complete. Your existing expedition progress has been preserved.");}
                    mode=Mode.COMPLETE;room=5;
                } else { room++;mode=Mode.TRAIL; }
    }
    private int openedChests() { return (int)claimed.stream().filter(id->id.startsWith("chest:")).count(); }
    private int availableChests() { return Math.max(0,(kills>=3?1:0)+cleared.size()+towerBest-openedChests()); }
    private Equipment item(String id) { return inventory.stream().filter(i->i.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Item not found.")); }
    private void drop(boolean boss) {
        if(!boss && random.nextInt(100)>=55)return;
        Equipment item=towerFloor>0
            ? Equipment.drop(random,Math.min(player.getLevel(),enemy.getLevel()),boss,player.getPlayerClass())
            : route()!=null?routeDrop(boss,route()):Equipment.regionalDrop(random,player.getLevel(),boss,player.getPlayerClass(),area);
        if(inventory.size()>=30) {rewardAdd("sold",1);player.addCoins(item.value());say("Pack full: sold "+item.name()+" for "+item.value()+" coins.");}
        else {inventory.add(item);if(claimed.contains("mission:ledger"))claimed.add("mission:loot:"+item.id());say("Found "+item.rarity().name().toLowerCase(Locale.ROOT)+" "+item.name()+". Equip it when you return to town.");}
    }
    private void applyEquipment() {
        int weapon=0,armour=0;player.equipAttribute(WeaponAttribute.NONE);
        for(Equipment item:inventory)if(item.id().equals(equipped.get(item.slot()))) {
            if(item.slot()==Equipment.Slot.WEAPON){weapon+=item.power();player.equipAttribute(item.attribute());}else armour+=item.power();
        }
        player.equipBonuses(weapon,armour);player.equipCrownArmour(inventory.stream().anyMatch(e->e.crownArmour()&&e.id().equals(equipped.get(Equipment.Slot.ARMOUR))));
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
        inTown();if(health<=0)applyDeathPenalty();player.setHealth(Math.max(1,health));player.setResource(resource);player.setPotions(Math.max(0,player.getPotions()-potionsUsed));
        if(won){player.gainXp(xp);player.addCoins(coins);kills++;if(inventory.size()<30)inventory.add(reward);else{player.addCoins(reward.value());say("Co-op reward sold because your bag was full.");}say("Co-op dungeon cleared: +"+xp+" XP, +"+coins+" coins and "+reward.name()+".");}
        else say("Returned from the co-op dungeon. Rest before another expedition.");
    }
    public int completeCoopStory(int index) {
        SubArea path=SubArea.get(index);require(path.unlocked(claimed,cleared),"Story route is locked.");
        boolean first=!path.complete(claimed,cleared);claimed.add(path.key());
        int bonus=first?Balance.storyClearXp(player.getLevel(),player.getXp(),path.index()):0;
        if(first){player.gainXp(bonus);say(StoryConsequences.ending(path.index(),path.ending(),claimed));say("Shared story clear: +"+bonus+" XP.");}
        if(index%3==2){cleared.add(path.area().name());say("Boss trophy: +1 "+BossMaterials.award(claimed,index)+". Use it at the Forge.");}
        player.configureBuild(claimed,cleared);return bonus;
    }
    public GameSave snapshot() {
        GameSave.PlayerData pd=new GameSave.PlayerData(player.getName(),player.getPlayerClass(),player.getLevel(),player.getXp(),player.getHealth(),player.getResource(),player.getCoins(),player.getPotions(),player.getSwordDamage());
        GameSave.BattleData battle=mode==Mode.COMBAT?new GameSave.BattleData(enemy.getName(),enemy.getLevel(),enemy.getMaxHealth(),enemy.getHealth(),enemy.getAttackPower(),enemy.getDefence(),enemy.isBoss(),combat.getRound(),combat.snapshotCooldowns(),player.snapshotStatuses(),enemy.snapshotStatuses()):null;
        return new GameSave(1,pd,mode.name(),area,room,kills,deaths,towerBest,towerFloor,Set.copyOf(cleared),Set.copyOf(claimed),List.copyOf(inventory),Map.copyOf(equipped),List.copyOf(log),battle);
    }
    public static GameSession restore(GameSave save, Random random) {
        if(save.schemaVersion()!=1)throw new IllegalArgumentException("Unsupported save version.");
        GameSave.PlayerData pd=save.player();Player p=pd.type().create(pd.name());
        p.setLevel(pd.level());p.configureBuild(save.claimed(),save.cleared());p.setXp(pd.xp());p.setHealth(pd.health());p.setResource(pd.resource());p.setCoins(pd.coins());p.setPotions(pd.potions());p.setSwordDamage(pd.swordDamage());
        GameSession game=new GameSession(p,random);
        game.mode=Mode.valueOf(save.mode());game.area=save.area();game.room=save.room();game.kills=save.kills();game.deaths=save.deaths();
        game.towerBest=save.towerBest();game.towerFloor=save.towerFloor();game.cleared.addAll(save.cleared());game.claimed.addAll(save.claimed());
        game.inventory.addAll(save.inventory());game.equipped.putAll(save.equipped());game.applyEquipment();game.log.clear();game.log.addAll(save.log());
        if(game.mode==Mode.COMBAT) {
            GameSave.BattleData b=Objects.requireNonNull(save.battle());int xp=Balance.enemyXp(b.level())*(b.boss()?2:1);
            game.enemy=new Enemy(b.name(),b.level(),b.maxHealth(),b.attack(),b.defence(),game.rivalActive()?0:4+b.level(),game.rivalActive()?0:8+b.level()*2,game.rivalActive()?0:xp,game.rivalActive()?0:xp);
            if(b.boss())game.enemy.asBoss();game.enemy.restoreHealth(b.health());game.combat=new CombatEngine(p,game.enemy,random);
            game.combat.restoreProgress(b.round(),b.cooldowns());p.restoreStatuses(b.playerStatuses());game.enemy.restoreStatuses(b.enemyStatuses());
        }
        return game;
    }
    public Map<String,Object> view() {
        Map<String,Object> view=new LinkedHashMap<>();
        view.put("player",Map.ofEntries(Map.entry("name",player.getName()),Map.entry("type",player.getPlayerClass()),Map.entry("rank",WorldNames.rank(player.getPlayerClass(),cleared)),Map.entry("title",cleared.contains(Area.DRAGONS_SPIRE.name())?"Dragonbane":""),Map.entry("level",player.getLevel()),Map.entry("xp",player.getXp()),Map.entry("nextXp",player.getXpToNextLevel()),Map.entry("health",player.getHealth()),Map.entry("maxHealth",player.getMaxHealth()),Map.entry("resource",player.getResource()),Map.entry("maxResource",player.getMaxResource()),Map.entry("coins",player.getCoins()),Map.entry("potions",player.getPotions()),Map.entry("attack",player.getAttackPower()),Map.entry("defence",player.getDefence()),Map.entry("statuses",player.getStatuses())));
        view.put("deathPenalty",Map.of("xpPercent",15+5*highestRoute().area().ordinal(),"coinPercent",10+5*highestRoute().area().ordinal(),"materialPercent",5+5*highestRoute().area().ordinal()));
        if(mode==Mode.COMPLETE&&claimed.contains("mission:ledger")&&route()!=null){int region=route().index()/3;view.put("expeditionRewards",Map.of("xp",missionValue("reward_xp",0),"coins",missionValue("reward_coins",0),"sold",missionValue("reward_sold",0),"items",inventory.stream().filter(i->claimed.contains("mission:loot:"+i.id())).toList(),"trophy",route().index()%3==2?BossMaterials.name(region):"","trophyOwned",BossMaterials.owned(claimed,region)));}
        view.put("rivals",RivalEncounters.view(claimed,cleared));view.put("rivalActive",rivalActive());
        view.put("npcQuests",NpcQuests.view(claimed,cleared));
        view.put("recipes",Crafting.view(player,claimed,cleared));
        view.put("gathering",Arrays.stream(Gathering.values()).map(g->g.view(claimed,cleared)).toList());
        if(missionActive()&&Set.of(Mode.COMBAT,Mode.OBJECTIVE).contains(mode)){MissionObjective objective=StoryConsequences.objective(route().index(),claimed);view.put("missionObjective",Map.of("title",objective.title,"action",objective.action,"progress",missionValue("work",0),"integrity",missionValue("integrity",100),"escort",objective==MissionObjective.ESCORT,"help",objective==MissionObjective.ESCORT?"Escort twice per encounter. Each escort restores 15 integrity; other turns cost 5. Work takes your combat turn.":objective==MissionObjective.SEAL?"Disrupt twice per encounter. Disrupting halves incoming damage that turn, but deals no damage.":"Search twice per encounter. Searching takes your turn while the enemy can attack. Finish searching after battle if needed."));}
        view.put("story",StoryPath.view(claimed,cleared));view.put("storyDecision",StoryConsequences.view(claimed,cleared));view.put("storyCatchupXp",StoryConsequences.catchupXp(player,claimed,cleared));
        view.put("routes",SubArea.ALL.stream().map(r->Map.of("index",r.index(),"area",r.area(),"name",r.name(),"minLevel",r.minLevel(),"maxLevel",r.maxLevel(),"mission",r.mission(),"unlocked",r.unlocked(claimed,cleared),"complete",r.complete(claimed,cleared))).toList());
        if(route()!=null)view.put("route",Map.of("name",route().name(),"mission",route().mission(),"opening",route().opening(),"ending",StoryConsequences.ending(route().index(),route().ending(),claimed),"guardian",route().guardian()));
        view.put("storyArchive",SubArea.ALL.stream().filter(r->r.complete(claimed,cleared)).map(r->Map.of("name",r.name(),"text",StoryConsequences.ending(r.index(),r.ending(),claimed))).toList());
        view.put("mode",mode);view.put("area",area);view.put("room",room);view.put("towerFloor",towerFloor);view.put("towerBest",towerBest);
        view.put("kills",kills);view.put("deaths",deaths);view.put("log",List.copyOf(log));view.put("cleared",Set.copyOf(cleared));view.put("claimed",Set.copyOf(claimed));
        view.put("chests",availableChests());view.put("chestEpicChance",Math.max(highestRoute().minLevel(),Math.min(highestRoute().maxLevel(),player.getLevel()))>=31?8:0);view.put("shopPrice",20+shopLevel()*6);view.put("shopLevel",shopLevel());
        view.put("inventory",inventory.stream().map(i->Map.of("item",i,"equipped",i.id().equals(equipped.get(i.slot())),"value",i.value(),"upgradeCost",i.upgradeCost(),"attributeDescription",i.effectDescription(),"protected",claimed.contains("keep:"+i.id()))).toList());
        view.put("regions",Campaign.REGIONS.stream().map(r->Map.of("id",r.area(),"name",r.area().getDisplayName(),"subtitle",r.subtitle(),"story",r.story(),"boss",r.boss(),"quest",r.quest(),"minLevel",r.area().getMinLevel(),"maxLevel",r.area().getMaxLevel(),"unlocked",unlocked(r.area()))).toList());
        view.put("build",CharacterBuild.view(player,claimed,cleared));
        view.put("abilities",player.getAvailableAbilities().stream().map(a->Map.of("id",a.id(),"name",a.name(),"cost",a.cost(),"unlockLevel",a.unlockLevel(),"cooldown",combat==null?0:combat.getCooldown(a.id()),"baseCooldown",a.cooldown())).toList());
        if(mode==Mode.COMBAT)view.put("enemy",Map.of("name",enemy.getName(),"level",enemy.getLevel(),"health",enemy.getHealth(),"maxHealth",enemy.getMaxHealth(),"boss",enemy.isBoss(),"intent",enemy.intent(combat.getRound()),"advice",enemy.advice(combat.getRound()),"statuses",enemy.getStatuses(),"phase",enemy.phase()));
        return view;
    }
}
