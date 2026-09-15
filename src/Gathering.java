import java.util.*;

/** Per-character professions stored with the existing versioned save flags. */
public enum Gathering {
    FORAGING("The Blighted Hollow","Woodcutting & foraging",0,new String[]{"Fallen timber","Hardwood stand","Heartwood grove"},new String[]{"Timber","Hardwood","Heartwood"}),
    MINING("The Hollow Vein","Mining",3,new String[]{"Surface seam","Iron gallery","Deep crystal seam"},new String[]{"Stone","Iron ore","Ward crystal"}),
    FISHING("The Drowned Archive","Fishing island",6,new String[]{"Island shallows","Reed inlet","Archive depths"},new String[]{"River fish","Silverfin","Deepwater pearl"});
    public final String name,label;public final int route;public final String[] sites,materials;
    Gathering(String name,String label,int route,String[] sites,String[] materials){this.name=name;this.label=label;this.route=route;this.sites=sites;this.materials=materials;}
    private String key(String kind){return "gather:"+name()+":"+kind+":";}
    private int number(Set<String> flags,String kind){String prefix=key(kind);return flags.stream().filter(f->f.startsWith(prefix)).mapToInt(f->Integer.parseInt(f.substring(prefix.length()))).findFirst().orElse(0);}
    private void put(Set<String> flags,String kind,int value){String prefix=key(kind);flags.removeIf(f->f.startsWith(prefix));flags.add(prefix+value);}
    public int owned(Set<String> flags,int tier){return number(flags,"item"+tier);}
    public void consume(Set<String> flags,int tier,int amount){CoopDungeon.require(amount>=0&&owned(flags,tier)>=amount,"Not enough materials.");put(flags,"item"+tier,owned(flags,tier)-amount);}
    public int level(Set<String> flags){return 1+number(flags,"xp")/100;}
    private boolean open(int tier,Set<String> flags,Set<String> cleared){return SubArea.get(route+tier).unlocked(flags,cleared)&&level(flags)>=1+tier*3;}
    public Map<String,Object> view(Set<String> flags,Set<String> cleared){
        List<Map<String,Object>> choices=new ArrayList<>();for(int i=0;i<3;i++)choices.add(Map.of("tier",i,"name",sites[i],"material",materials[i],"owned",number(flags,"item"+i),"unlocked",open(i,flags,cleared),"requiredLevel",1+i*3,"route",SubArea.get(route+i).name()));
        return Map.of("id",name(),"name",name,"label",label,"level",level(flags),"xp",number(flags,"xp")%100,"mastered",level(flags)==10,"open",SubArea.get(route).unlocked(flags,cleared),"sites",choices);
    }
    public String gather(int tier,Player p,Set<String> flags,Set<String> cleared,Random random){
        CoopDungeon.require(tier>=0&&tier<3&&open(tier,flags,cleared),"Open the story route and reach the required profession level first.");
        CoopDungeon.require(p.getResource()>=8,"Recover at the inn before gathering again.");
        CoopDungeon.require(number(flags,"item"+tier)<999,"Sell some of this material before gathering more.");
        p.spendResource(8);int amount=1+random.nextInt(3);amount=Math.min(amount,999-number(flags,"item"+tier));put(flags,"item"+tier,number(flags,"item"+tier)+amount);
        int before=level(flags);put(flags,"xp",Math.min(900,number(flags,"xp")+25));
        return "Gathered "+amount+" "+materials[tier]+". "+(before<10?"+25 profession XP.":"Profession mastered.")+(level(flags)>before?" Profession level "+level(flags)+"!":"");
    }
    public String sell(int tier,Player p,Set<String> flags){CoopDungeon.require(tier>=0&&tier<3,"Choose a material.");int owned=number(flags,"item"+tier);CoopDungeon.require(owned>=5,"You need five materials to sell a bundle.");int value=10+10*tier;p.addCoins(value);put(flags,"item"+tier,owned-5);return "Sold five "+materials[tier]+" for "+value+" coins.";}
}
