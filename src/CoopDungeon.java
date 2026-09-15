import java.util.*;

/** A persisted two-player encounter. No UI or network dependencies. */
public class CoopDungeon {
    public String state="LOBBY",leader;
    public int round,enemyHealth,enemyMaxHealth,level;
    public long deadline;
    public int rescuesUsed;
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
        Player player(){Player p=original.player().type().create(original.player().name());p.setLevel(original.player().level());p.setHealth(health);p.setResource(resource);p.setPotions(potions);int weapon=0,armour=0;
            for(Equipment e:original.inventory())if(e.id().equals(original.equipped().get(e.slot()))){if(e.slot()==Equipment.Slot.WEAPON){weapon=e.power();p.equipAttribute(e.attribute());}else armour=e.power();}
            p.equipBonuses(weapon,armour);p.setSwordDamage(original.player().swordDamage());p.restoreStatuses(statuses);return p;}
        void store(Player p){health=p.getHealth();resource=p.getResource();potions=p.getPotions();statuses=p.snapshotStatuses();}
    }
    public void add(String user,GameSave save){require(state.equals("LOBBY")&&members.size()<2,"This party is full or already started.");require(save.mode().equals("TOWN")&&save.player().health()>0,"Return to town and recover before joining.");if(!members.isEmpty())require(Math.abs(members.get(0).original.player().level()-save.player().level())<=3,"Party members must be within three levels of each other.");members.add(new Member(user,save));if(leader==null)leader=user;}
    public void start(String user,long now){require(user.equals(leader),"Only the host can start.");require(state.equals("LOBBY")&&members.size()==2,"Two players are needed.");level=members.stream().mapToInt(m->m.original.player().level()).max().orElse(1);enemyMaxHealth=160+28*(level-1);enemyHealth=enemyMaxHealth;state="BATTLE";deadline=now+60000;say("The Rootbound Warden blocks the ruined gate. Choose moves together. Protect can cover your partner.");}
    public String intent(){String target=members.size()<2?"your party":members.get(round%2).original.player().name();return round%3==2?"Heavy strike against "+target+" — defend or have your partner protect you.":round%3==1?"Root sweep hits both players.":"Strike against "+target+".";}
    public void choose(String user,int expectedRound,String action,long now,Random random){require(state.equals("BATTLE")&&round==expectedRound,"The round has changed. Refresh your party.");Member member=members.stream().filter(m->m.username.equals(user)).findFirst().orElseThrow();require(member.action==null,"Your move is already locked in.");validate(member,action);member.action=action;if(members.stream().allMatch(m->m.action!=null))resolve(now,random);}
    public void cover(int expectedRound,long now,Random random){require(state.equals("BATTLE")&&round==expectedRound,"The round has changed.");require(now>=deadline,"Give your partner a minute to choose.");require(members.stream().anyMatch(m->m.action!=null),"Choose your own move first.");for(Member m:members)if(m.action==null)m.action="DEFEND";say("The missing move defaults to defend.");resolve(now,random);}
    private void validate(Member m,String action){require(action!=null,"Choose a move.");Player p=m.player();if(action.startsWith("ability:")){Ability a=p.getAvailableAbilities().stream().filter(x->x.id().equals(action.substring(8))).findFirst().orElse(null);require(a!=null&&m.cooldowns.getOrDefault(a.id(),0)==0&&p.getResource()>=a.cost(),"Ability unavailable or insufficient resource.");}else{require(Set.of("ATTACK","DEFEND","PROTECT","MEND","POTION").contains(action),"Unknown party move.");if(action.equals("MEND"))require(p.getResource()>=12,"Mend needs 12 resource.");if(action.equals("POTION"))require(p.getPotions()>0&&p.getHealth()<p.getMaxHealth(),"No potion needed or available.");}}
    private void resolve(long now,Random random){
        Player[] players=members.stream().map(Member::player).toArray(Player[]::new);
        Enemy enemy=new Enemy("Rootbound Warden",level,enemyMaxHealth,16+3*(level-1),5+level,0,0,0,0);enemy.restoreHealth(enemyHealth);enemy.restoreStatuses(enemyStatuses);
        boolean[] guard=new boolean[2];int[] protector={-1,-1};
        // Both players commit before resolution; defensive support is applied before attacks.
        for(int i=0;i<2;i++){String action=members.get(i).action;if(action.equals("DEFEND"))guard[i]=true;if(action.equals("PROTECT")){guard[i]=true;protector[1-i]=i;}}
        for(int i=0;i<2;i++){Member m=members.get(i);Player p=players[i];String action=m.action;int before=enemy.getHealth();m.cooldowns.replaceAll((id,n)->Math.max(0,n-1));List<String> effects=new ArrayList<>();CombatAction kind=CombatAction.ATTACK;
            switch(action){
                case "ATTACK" -> {if(!enemy.isDead())say(p.getName()+" strikes for "+enemy.receiveDamage(p.rollDamage(random,-2,2),DamageType.PHYSICAL)+".");}
                case "DEFEND","PROTECT" -> {kind=CombatAction.DEFEND;say(p.getName()+(action.equals("PROTECT")?" protects their partner.":" defends."));}
                case "MEND" -> {kind=CombatAction.ABILITY;p.spendResource(12);Player partner=players[1-i];int hp=partner.getHealth();partner.heal(Math.max(10,partner.getMaxHealth()/5));say(p.getName()+" mends "+partner.getName()+" for "+(partner.getHealth()-hp)+" health.");}
                case "POTION" -> {kind=CombatAction.POTION;p.usePotion();say(p.getName()+" drinks a potion.");}
                default -> {kind=CombatAction.ABILITY;Ability a=p.getAvailableAbilities().stream().filter(x->x.id().equals(action.substring(8))).findFirst().orElseThrow();p.spendResource(a.cost());if(!enemy.isDead())a.effect().apply(p,enemy,random,effects);m.cooldowns.put(a.id(),a.cooldown());}
            }
            p.getWeaponAttribute().apply(kind,before-enemy.getHealth(),p,enemy,effects);for(String line:effects)say(p.getName()+": "+line);p.endTurn();
        }
        if(!enemy.isDead()){
            if(enemy.hasStatus(StatusEffect.Kind.STUN))say("The Warden is stunned; its attack is interrupted.");
            else {int[] targets=round%3==1?new int[]{0,1}:new int[]{round%2};for(int target:targets){int raw=16+3*(level-1);if(round%3==2)raw*=2;if(round%3==1)raw=(int)Math.round(raw*.75);int recipient=protector[target]>=0?protector[target]:target;int damage=players[recipient].receiveDamage(guard[recipient]?(raw+1)/2:raw,DamageType.PHYSICAL);say("The Warden hits "+players[recipient].getName()+" for "+damage+".");}}
            enemy.endTurn();
        }
        enemyStatuses=enemy.snapshotStatuses();enemyHealth=enemy.getHealth();for(int i=0;i<2;i++){players[i].restoreResource(4);members.get(i).store(players[i]);members.get(i).action=null;}
        round++;deadline=now+60000;
        if(Arrays.stream(players).anyMatch(Player::isDead)){
            if(Arrays.stream(players).filter(Player::isDead).count()==1&&rescuesUsed==0){state="RESCUE";deadline=now+60000;say("A partner is down! The survivor has one minute to rescue them. One rescue per run.");}
            else{state="DEFEAT";say("Your party fell. The wardens return you to town without clear rewards.");}
        }
        else if(enemy.isDead()){state="VICTORY";say("The Warden falls! Each player earns an individual rare-or-epic item, "+xpReward()+" XP and "+coinReward()+" coins.");}
    }
    public void rescue(String user,long now){
        require(state.equals("RESCUE"),"No rescue is pending.");
        Member survivor=members.stream().filter(m->m.username.equals(user)).findFirst().orElseThrow();
        require(survivor.health>0,"Your partner must rescue you.");
        if(now>deadline){state="DEFEAT";say("The rescue window closed. The wardens return you both to town.");return;}
        Member fallen=members.stream().filter(m->m.health==0).findFirst().orElseThrow();
        Player p=fallen.player();p.setHealth(Math.max(1,p.getMaxHealth()*35/100));p.clearStatuses();fallen.store(p);rescuesUsed++;
        state=enemyHealth==0?"VICTORY":"BATTLE";deadline=now+60000;say(survivor.original.player().name()+" rescued "+p.getName()+" at 35% health. The rescue is spent for this run.");
    }
    public void endRescue(long now){require(state.equals("RESCUE"),"No rescue is pending.");require(now>deadline,"Your partner still has time to rescue you.");state="DEFEAT";say("The rescue window closed. Returned to town.");}
    public int xpReward(){return 40+12*(level-1);}public int coinReward(){return 40+8*(level-1);}
    public void say(String line){log.add(line);while(log.size()>25)log.remove(0);}
    public static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
}
