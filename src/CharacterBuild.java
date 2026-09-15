import java.util.*;

/** Server-owned build configuration; persisted with the character's existing flags. */
public final class CharacterBuild {
    private CharacterBuild() {}
    public enum Attribute { POWER, VITALITY, ARMOUR, FOCUS }
    public enum Specialisation {
        VANGUARD(PlayerClass.WARRIOR,"Vanguard","+15% armour; +10% maximum health",0,10,15,0),
        CHAMPION(PlayerClass.WARRIOR,"Champion","+12% direct ability damage",12,0,0,0),
        BATTLE_MAGE(PlayerClass.MAGE,"Battle Mage","+12% direct ability damage",12,0,0,0),
        SPELLWARDEN(PlayerClass.MAGE,"Spellwarden","+15% armour; +20 maximum resource",0,0,15,20),
        LIGHTWARDEN(PlayerClass.CLERIC,"Lightwarden","+20% direct healing",0,0,0,0),
        INQUISITOR(PlayerClass.CLERIC,"Inquisitor","+12% direct ability damage",12,0,0,0),
        ASSASSIN(PlayerClass.ROGUE,"Assassin","+12% direct ability damage",12,0,0,0),
        PATHFINDER(PlayerClass.ROGUE,"Pathfinder","+10% maximum health; +20 maximum resource",0,10,0,20);
        public final PlayerClass type;public final String title,description;public final int damage,health,armour,resource;
        Specialisation(PlayerClass type,String title,String description,int damage,int health,int armour,int resource){this.type=type;this.title=title;this.description=description;this.damage=damage;this.health=health;this.armour=armour;this.resource=resource;}
    }
    static int value(Set<String> flags,String key){String prefix="build:"+key+":";return flags.stream().filter(f->f.startsWith(prefix)).mapToInt(f->Integer.parseInt(f.substring(prefix.length()))).findFirst().orElse(0);}
    static void put(Set<String> flags,String key,int value){String prefix="build:"+key+":";flags.removeIf(f->f.startsWith(prefix));flags.add(prefix+value);}
    public static Specialisation specialisation(Set<String> flags){return flags.stream().filter(f->f.startsWith("build:spec:")).map(f->Specialisation.valueOf(f.substring(11))).findFirst().orElse(null);}
    public static int remaining(Player p,Set<String> flags){return Math.max(0,2*(p.getLevel()-1)-Arrays.stream(Attribute.values()).mapToInt(a->value(flags,a.name())).sum());}
    public static int skillPoints(Player p,Set<String> flags){return Math.max(0,p.getLevel()/3-p.getAllBuildAbilities().stream().mapToInt(a->value(flags,"skill:"+a.id())).sum());}
    public static void command(String action,String choice,Player p,Set<String> flags,Set<String> cleared){
        switch(action){
            case "attribute" -> {Attribute a=Attribute.valueOf(choice);CoopDungeon.require(remaining(p,flags)>0&&value(flags,a.name())<60,"No point available or attribute cap reached.");put(flags,a.name(),value(flags,a.name())+1);}
            case "specialise" -> {Specialisation spec=Specialisation.valueOf(choice);CoopDungeon.require(spec.type==p.getPlayerClass()&&cleared.contains(Area.WHISPERING_WOODS.name()),"Earn your first regional promotion before specialising.");flags.removeIf(f->f.startsWith("build:spec:"));flags.add("build:spec:"+spec.name());}
            case "upgrade_ability" -> {Ability a=p.getAllBuildAbilities().stream().filter(x->x.id().equals(choice)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown ability."));CoopDungeon.require(p.abilityUnlocked(a)&&skillPoints(p,flags)>0&&value(flags,"skill:"+choice)<3,"Ability locked, rank capped or no training points.");put(flags,"skill:"+choice,value(flags,"skill:"+choice)+1);}
            case "loadout" -> {String[] ids=choice.split(",",-1);Set<String> unique=new HashSet<>(Arrays.asList(ids));CoopDungeon.require(ids.length>0&&ids.length<=4&&unique.size()==ids.length,"Choose one to four distinct abilities.");for(String id:ids)CoopDungeon.require(p.getAllBuildAbilities().stream().anyMatch(a->a.id().equals(id)&&p.abilityUnlocked(a)),"Loadout contains a locked or unknown ability.");flags.removeIf(f->f.startsWith("build:slot:"));for(String id:ids)flags.add("build:slot:"+id);}
            case "reset_build" -> flags.removeIf(f->f.startsWith("build:"));
            default -> throw new IllegalArgumentException("Unknown build action.");
        }
        p.configureBuild(flags,cleared);
    }
    public static Map<String,Object> view(Player p,Set<String> flags,Set<String> cleared){
        Map<String,Object> out=new LinkedHashMap<>();out.put("points",remaining(p,flags));out.put("skillPoints",skillPoints(p,flags));out.put("attributes",Arrays.stream(Attribute.values()).map(a->Map.of("id",a.name(),"points",value(flags,a.name()))).toList());
        Specialisation spec=specialisation(flags);out.put("specialisation",spec==null?"None":spec.title);out.put("canSpecialise",cleared.contains(Area.WHISPERING_WOODS.name()));out.put("specialisations",Arrays.stream(Specialisation.values()).filter(s->s.type==p.getPlayerClass()).map(s->Map.of("id",s.name(),"name",s.title,"description",s.description)).toList());out.put("loadout",p.getAvailableAbilities().stream().map(Ability::id).toList());out.put("abilities",p.getAllBuildAbilities().stream().map(a->Map.of("id",a.id(),"name",a.name(),"rank",value(flags,"skill:"+a.id()),"unlocked",p.abilityUnlocked(a),"requirement",a.id().startsWith("trained_")?"Clear Stonefang Caves":a.id().startsWith("signature_")?"Choose a specialisation after clearing the Woods":"Level "+a.unlockLevel())).toList());return out;
    }
}
