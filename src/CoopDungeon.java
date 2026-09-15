import java.util.*;

/** A persisted two-player encounter. No UI or network dependencies. */
public class CoopDungeon {
    public String state="LOBBY",leader;
    public Integer storyRoute;
    public String approach="standard";
    public int stageStartRound;
    public int stage,objectiveProgress,comboReadyRound;
    public int integrity=100;
    public int round,enemyHealth,enemyMaxHealth,level;
    public long deadline;
    public int rescuesUsed;
    public boolean duoUnlocked,duoUsed;
    public record Reward(String name,int xp,int coins,Equipment item,int convertedCoins) {}
    public List<Reward> rewards=new ArrayList<>();
    public List<Member> members=new ArrayList<>();
    public List<Character.StatusState> enemyStatuses=new ArrayList<>();
    public List<String> log=new ArrayList<>();
    public static class Member {
        public String username,action;
        public GameSave original;
        public int health,resource,potions;
        public Map<String,Integer> cooldowns=new HashMap<>();
        public List<Character.StatusState> statuses=new ArrayList<>();
        public Member() {}
        Member(String username,GameSave save){this.username=username;original=save;health=save.player().health();resource=save.player().resource();potions=Math.min(3,save.player().potions());}
        Player player(){Player p=original.player().type().create(original.player().name());p.setLevel(original.player().level());p.configureBuild(original.claimed(),original.cleared());p.setHealth(health);p.setResource(resource);p.setPotions(potions);int weapon=0,armour=0;
            for(Equipment e:original.inventory())if(e.id().equals(original.equipped().get(e.slot()))){if(e.slot()==Equipment.Slot.WEAPON){weapon=e.power();p.equipAttribute(e.attribute());}else armour=e.power();}
            p.equipBonuses(weapon,armour);p.setSwordDamage(original.player().swordDamage());p.restoreStatuses(statuses);return p;}
        void store(Player p){health=p.getHealth();resource=p.getResource();potions=p.getPotions();statuses=p.snapshotStatuses();}
    }
    public String missionName(){return storyRoute==null?"The Rootbound Gate":SubArea.get(storyRoute).name();}
    public String enemyName(){return storyRoute==null?"Rootbound Warden":stage==2?SubArea.get(storyRoute).guardian():Campaign.region(SubArea.get(storyRoute).area()).enemies()[stage];}
    public MissionObjective objective(){return storyRoute==null?null:MissionObjective.forRoute(storyRoute);}
    public void selectStory(int index){require(members.isEmpty()&&index>=0&&index<SubArea.ALL.size(),"Choose a story route.");storyRoute=index;}
    public void chooseApproach(String user,String choice){require(state.equals("LOBBY")&&user.equals(leader)&&storyRoute!=null,"Only the host can set a story approach before departure.");require(Set.of("standard","rescue","scout").contains(choice),"Choose an approach.");approach=choice;}
    private void prepareStage(){stageStartRound=round;enemyMaxHealth=stage==2?150+22*(level-1):90+15*(level-1);enemyHealth=enemyMaxHealth;enemyStatuses=new ArrayList<>();objectiveProgress=0;say("Stage "+(stage+1)+"/3: "+enemyName()+". "+objective().help);}
    private void stageCleared(long now){
        if(storyRoute!=null && objectiveProgress<3){say("The enemy is down. Finish the mission objective to continue.");return;}
        if(storyRoute!=null&&stage<2){stage++;prepareStage();for(Member m:members){Player p=m.player();p.heal(p.getMaxHealth()/5);p.restoreResource(12);m.store(p);}say("The company regroups: +20% health and 12 resource.");deadline=now+60000;}
        else{state="VICTORY";say(storyRoute==null?"The Warden falls! Individual rewards are ready.":SubArea.get(storyRoute).ending());}
    }
    public void add(String user,GameSave save){if(storyRoute!=null)require(SubArea.get(storyRoute).unlocked(save.claimed(),save.cleared()),"This story route must be unlocked for both characters. Choose the earlier player's route.");require(state.equals("LOBBY")&&members.size()<2,"This party is full or already started.");require(save.mode().equals("TOWN")&&save.player().health()>0,"Return to town and recover before joining.");if(!members.isEmpty())require(Math.abs(members.get(0).original.player().level()-save.player().level())<=3,"Party members must be within three levels of each other.");members.add(new Member(user,save));if(leader==null)leader=user;}
    public void start(String user,long now){require(user.equals(leader),"Only the host can start.");require(state.equals("LOBBY")&&members.size()==2,"Two players are needed.");if("rescue".equals(approach)){require(members.stream().allMatch(m->m.resource>=12),"Both partners need 12 resource for relief duty.");for(Member m:members)m.resource-=12;say("Relief duty: you escort survivors alongside the mission. Completion earns extra coins.");}else if("scout".equals(approach))say("Scouted approach: guard on the first turn of each stage; completion pays 20% fewer coins.");level=members.stream().mapToInt(m->m.original.player().level()).max().orElse(1);state="BATTLE";deadline=now+60000;
        if(storyRoute!=null){SubArea route=SubArea.get(storyRoute);level=Math.max(route.minLevel(),Math.min(route.maxLevel(),level));say(route.opening());prepareStage();}
        else{enemyMaxHealth=160+28*(level-1);enemyHealth=enemyMaxHealth;say("The Rootbound Warden blocks the ruined gate. Choose moves together.");}}
    public boolean regionalBoss(){return storyRoute!=null&&stage==2&&storyRoute%3==2;}
    private Enemy bossEnemy(){Enemy e=new Enemy(enemyName(),level,enemyMaxHealth,16+3*(level-1),5+level,0,0,0,0).asBoss();e.restoreHealth(enemyHealth);return e;}
    public String phase(){return regionalBoss()?bossEnemy().phase():"";}
    public String intent(){if(regionalBoss()&&enemyHealth>0)return bossEnemy().intent(round)+" · Target: "+members.get(round%2).original.player().name()+". Protect can intercept; both guard against heavy sweeps.";if(enemyHealth==0)return "Enemy defeated — complete the objective.";String target=members.size()<2?"your party":members.get(round%2).original.player().name();return round%3==2?"Heavy strike against "+target+" — defend or have your partner protect you.":round%3==1?"Root sweep hits both players.":"Strike against "+target+".";}
    public void choose(String user,int expectedRound,String action,long now,Random random){require(state.equals("BATTLE")&&round==expectedRound,"The round has changed. Refresh your party.");Member member=members.stream().filter(m->m.username.equals(user)).findFirst().orElseThrow();require(member.action==null,"Your move is already locked in.");validate(member,action);member.action=action;if(members.stream().allMatch(m->m.action!=null))resolve(now,random);}
    public void cover(int expectedRound,long now,Random random){require(state.equals("BATTLE")&&round==expectedRound,"The round has changed.");require(now>=deadline,"Give your partner a minute to choose.");require(members.stream().anyMatch(m->m.action!=null),"Choose your own move first.");for(Member m:members)if(m.action==null)m.action="DEFEND";say("The missing move defaults to defend.");resolve(now,random);}
    private void validate(Member m,String action){require(action!=null,"Choose a move.");Player p=m.player();if(action.startsWith("ability:")){Ability a=p.getAvailableAbilities().stream().filter(x->x.id().equals(action.substring(8))).findFirst().orElse(null);require(a!=null&&m.cooldowns.getOrDefault(a.id(),0)==0&&p.getResource()>=a.cost(),"Ability unavailable or insufficient resource.");}else{require(Set.of("ATTACK","DEFEND","PROTECT","MEND","POTION","RALLY","OBJECTIVE").contains(action),"Unknown party move.");if(action.equals("OBJECTIVE"))require(storyRoute!=null&&(objectiveProgress<3||objective()==MissionObjective.ESCORT),"No objective work is needed.");if(action.equals("RALLY"))require(duoUnlocked&&!duoUsed,"Rally requires bond milestone 3 and can be used once per run.");if(action.equals("MEND"))require(p.getResource()>=12,"Mend needs 12 resource.");if(action.equals("POTION"))require(p.getPotions()>0&&p.getHealth()<p.getMaxHealth(),"No potion needed or available.");}}
    private void resolve(long now,Random random){
        Player[] players=members.stream().map(Member::player).toArray(Player[]::new);
        int bossAttack=16+3*(level-1);if(regionalBoss()&&objective()==MissionObjective.SEAL&&members.stream().anyMatch(m->"OBJECTIVE".equals(m.action)))bossAttack=(bossAttack+1)/2;
        Enemy enemy=new Enemy(enemyName(),level,enemyMaxHealth,bossAttack,5+level,0,0,0,0);enemy.restoreHealth(enemyHealth);enemy.restoreStatuses(enemyStatuses);if(regionalBoss())enemy.asBoss();Enemy.Move planned=enemy.move(round);
        int workers=(int)members.stream().filter(m->"OBJECTIVE".equals(m.action)).count();
        boolean[] guard=new boolean[2];if("scout".equals(approach)&&round==stageStartRound)Arrays.fill(guard,true);int[] protector={-1,-1};
        // Both players commit before resolution; defensive support is applied before attacks.
        for(int i=0;i<2;i++){String action=members.get(i).action;if(action.equals("DEFEND"))guard[i]=true;if(action.equals("PROTECT")){guard[i]=true;protector[1-i]=i;}}
        for(int i=0;i<2;i++){Member m=members.get(i);Player p=players[i];String action=m.action;int before=enemy.getHealth();m.cooldowns.replaceAll((id,n)->Math.max(0,n-1));List<String> effects=new ArrayList<>();CombatAction kind=CombatAction.ATTACK;
            switch(action){
                case "OBJECTIVE" -> {kind=CombatAction.ABILITY;objectiveProgress=Math.min(3,objectiveProgress+1);if(objective()==MissionObjective.ESCORT)integrity=Math.min(100,integrity+15);say(p.getName()+": "+objective().action+" ("+objectiveProgress+"/3).");}
                case "ATTACK" -> {if(!enemy.isDead())say(p.getName()+" strikes for "+enemy.receiveDamage(p.rollDamage(random,-2,2),DamageType.PHYSICAL)+".");}
                case "DEFEND","PROTECT" -> {kind=CombatAction.DEFEND;say(p.getName()+(action.equals("PROTECT")?" protects their partner.":" defends."));}
                case "MEND" -> {kind=CombatAction.ABILITY;p.spendResource(12);Player partner=players[1-i];int hp=partner.getHealth();partner.heal(Math.max(10,partner.getMaxHealth()/5));say(p.getName()+" mends "+partner.getName()+" for "+(partner.getHealth()-hp)+" health.");}
                case "RALLY" -> {kind=CombatAction.DEFEND;if(!duoUsed){duoUsed=true;for(Player partner:players){partner.heal(Math.max(1,partner.getMaxHealth()/10));partner.restoreResource(6);}say("Together: Rally restores 10% health and 6 resource to both partners.");}else say(p.getName()+" supports the rally.");}
                case "POTION" -> {kind=CombatAction.POTION;p.usePotion();say(p.getName()+" drinks a potion.");}
                default -> {kind=CombatAction.ABILITY;Ability a=p.getAvailableAbilities().stream().filter(x->x.id().equals(action.substring(8))).findFirst().orElseThrow();p.spendResource(a.cost());if(!enemy.isDead())a.effect().apply(p,enemy,random,effects);m.cooldowns.put(a.id(),a.cooldown());}
            }
            p.getWeaponAttribute().apply(kind,before-enemy.getHealth(),p,enemy,effects);for(String line:effects)say(p.getName()+": "+line);p.endTurn();
        }
        if(round>=comboReadyRound&&enemy.getHealth()<enemyHealth&&DuoCombos.eligible(players,members.get(0).action,members.get(1).action)){
            for(String line:DuoCombos.apply(players,enemy))say(line);comboReadyRound=round+3;
        }
        if(!enemy.isDead()){
            if(enemy.hasStatus(StatusEffect.Kind.STUN))say(enemyName()+" is stunned; its attack is interrupted.");
            else if(regionalBoss()){
                int target=round%2,recipient=protector[target]>=0?protector[target]:target;
                for(int i=0;i<2;i++)if(guard[i])players[i].applyStatus(new StatusEffect("party-guard",StatusEffect.Kind.GUARD,0,1));
                List<String> events=new ArrayList<>();
                enemy.performPlannedTurn(planned,players[recipient],random,events);
                if(planned==Enemy.Move.HEAVY){int other=1-recipient;int splash=players[other].receiveDamage(Math.max(1,bossAttack/2),DamageType.PHYSICAL);events.add("The shockwave hits "+players[other].getName()+" for "+splash+".");}
                for(String line:events)say(line);
            }
            else {int[] targets=round%3==1?new int[]{0,1}:new int[]{round%2};for(int target:targets){int raw=16+3*(level-1);if(round%3==2)raw*=2;if(round%3==1)raw=(int)Math.round(raw*.75);if(storyRoute!=null&&objective()==MissionObjective.SEAL&&workers>0)raw=(raw+1)/2;
                if(storyRoute!=null&&objective()==MissionObjective.INVESTIGATE&&"OBJECTIVE".equals(members.get(target).action))raw=(int)Math.round(raw*1.25);int recipient=protector[target]>=0?protector[target]:target;int damage=players[recipient].receiveDamage(guard[recipient]?(raw+1)/2:raw,DamageType.PHYSICAL);say(enemyName()+" hits "+players[recipient].getName()+" for "+damage+".");}}
            enemy.endTurn();
        }
        for(Player p:players)p.removeStatus("party-guard");
        enemyStatuses=enemy.snapshotStatuses();enemyHealth=enemy.getHealth();for(int i=0;i<2;i++){players[i].restoreResource(4);members.get(i).store(players[i]);members.get(i).action=null;}
        round++;deadline=now+60000;
        if(storyRoute!=null&&objective()==MissionObjective.ESCORT&&workers==0){integrity=Math.max(0,integrity-10);say("Unescorted supplies: integrity "+integrity+"/100.");}
        if(storyRoute!=null&&integrity==0){state="DEFEAT";say("The supply line is lost. Withdraw and try again; no story clear was awarded.");}
        else if(Arrays.stream(players).anyMatch(Player::isDead)){
            if(Arrays.stream(players).filter(Player::isDead).count()==1&&rescuesUsed==0){state="RESCUE";deadline=now+60000;say("A partner is down! The survivor has one minute to rescue them. One rescue per run.");}
            else{state="DEFEAT";say("Your party fell. The wardens return you to town without clear rewards.");}
        }
        else if(enemy.isDead())stageCleared(now);
    }
    public void rescue(String user,long now){
        require(state.equals("RESCUE"),"No rescue is pending.");
        Member survivor=members.stream().filter(m->m.username.equals(user)).findFirst().orElseThrow();
        require(survivor.health>0,"Your partner must rescue you.");
        if(now>deadline){state="DEFEAT";say("The rescue window closed. The wardens return you both to town.");return;}
        Member fallen=members.stream().filter(m->m.health==0).findFirst().orElseThrow();
        Player p=fallen.player();p.setHealth(Math.max(1,p.getMaxHealth()*35/100));p.clearStatuses();fallen.store(p);rescuesUsed++;
        state="BATTLE";if(enemyHealth==0)stageCleared(now);deadline=now+60000;say(survivor.original.player().name()+" rescued "+p.getName()+" at 35% health. The rescue is spent for this run.");
    }
    public void endRescue(long now){require(state.equals("RESCUE"),"No rescue is pending.");require(now>deadline,"Your partner still has time to rescue you.");state="DEFEAT";say("The rescue window closed. Returned to town.");}
    public int xpReward(){return 40+12*(level-1);}public int coinReward(){int base=40+8*(level-1);return "rescue".equals(approach)?base+20+level*2:"scout".equals(approach)?base*80/100:base;}
    public void say(String line){log.add(line);while(log.size()>25)log.remove(0);}
    public static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
}
