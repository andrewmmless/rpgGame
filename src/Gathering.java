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
    public int tool(Set<String> flags){return number(flags,"tool");}
    public String toolName(){return switch(this){case FISHING->"Fishing rod";case MINING->"Pickaxe";case FORAGING->"Forester's axe";};}
    public String upgrade(Player p,Set<String> flags,Set<String> cleared){int rank=tool(flags);CoopDungeon.require(rank<3,"Your tool is fully upgraded.");CoopDungeon.require(SubArea.get(route).unlocked(flags,cleared)&&level(flags)>=2+rank*3,"Reach the next profession milestone first.");int coins=20+rank*30,amount=8+rank*4;CoopDungeon.require(p.getCoins()>=coins&&owned(flags,0)>=amount,"You need more coins or basic materials.");p.spendCoins(coins);consume(flags,0,amount);put(flags,"tool",rank+1);return toolName()+" upgraded to rank "+(rank+1)+". Every trip gains one extra material per rank.";}
    public String findName(int tier){return switch(this){case FISHING->new String[]{"Amber trout","Moonfin carp","Crown pearl"}[tier];case MINING->new String[]{"Copper geode","Star iron","Ember crystal"}[tier];case FORAGING->new String[]{"Silverleaf","Singing acorn","Cinder orchid"}[tier];};}
    public Map<String,Object> progression(Set<String> flags){int rank=tool(flags);return Map.of("toolName",toolName(),"toolRank",rank,"toolLevel",2+rank*3,"toolCoins",20+rank*30,"toolMaterials",8+rank*4,"basicOwned",owned(flags,0),"collection",java.util.stream.IntStream.range(0,3).mapToObj(t->Map.of("name",findName(t),"found",flags.contains(key("find")+t),"progress",number(flags,"survey"+t),"site",sites[t])).toList(),"collectionComplete",java.util.stream.IntStream.range(0,3).allMatch(t->flags.contains(key("find")+t)));}
    public int level(Set<String> flags){return 1+number(flags,"xp")/100;}
    private boolean open(int tier,Set<String> flags,Set<String> cleared){return SubArea.get(route+tier).unlocked(flags,cleared)&&level(flags)>=1+tier*3;}
    public Map<String,Object> view(Set<String> flags,Set<String> cleared){
        List<Map<String,Object>> choices=new ArrayList<>();for(int i=0;i<3;i++)choices.add(Map.of("tier",i,"name",sites[i],"material",materials[i],"owned",number(flags,"item"+i),"unlocked",open(i,flags,cleared),"requiredLevel",1+i*3,"route",SubArea.get(route+i).name()));
        Map<String,Object> view=new LinkedHashMap<>(Map.of("id",name(),"name",name,"label",label,"level",level(flags),"xp",number(flags,"xp")%100,"mastered",level(flags)==10,"open",SubArea.get(route).unlocked(flags,cleared),"sites",choices));view.put("progression",progression(flags));return view;
    }
    public String gather(int tier,Player p,Set<String> flags,Set<String> cleared,Random random){
        return gather(tier,p,flags,cleared,random,false);
    }
    public String gather(int tier,Player p,Set<String> flags,Set<String> cleared,Random random,boolean survey){
        CoopDungeon.require(tier>=0&&tier<3&&open(tier,flags,cleared),"Open the story route and reach the required profession level first.");
        CoopDungeon.require(p.getResource()>=(survey?12:8),"Recover at the inn before gathering again.");
        CoopDungeon.require(number(flags,"item"+tier)<999,"Sell some of this material before gathering more.");
        p.spendResource(survey?12:8);int amount=(survey?1:1+random.nextInt(3))+tool(flags);amount=Math.min(amount,999-number(flags,"item"+tier));put(flags,"item"+tier,number(flags,"item"+tier)+amount);
        int before=level(flags);put(flags,"xp",Math.min(900,number(flags,"xp")+(survey?35:25)));
        String discovery="";if(survey&&!flags.contains(key("find")+tier)){int progress=Math.min(3,number(flags,"survey"+tier)+1);put(flags,"survey"+tier,progress);if(progress==3){flags.add(key("find")+tier);p.addCoins(15+15*tier);discovery=" Discovered "+findName(tier)+"! Collection reward: "+(15+15*tier)+" coins.";if(java.util.stream.IntStream.range(0,3).allMatch(t->flags.contains(key("find")+t))){put(flags,"tool",Math.min(3,tool(flags)+1));discovery+=" Collection complete: a free tool rank (up to rank 3).";}}else discovery=" Discovery progress: "+progress+"/3 at this site.";}

        return "Gathered "+amount+" "+materials[tier]+". "+(before<10?"+"+(survey?35:25)+" profession XP.":"Profession mastered.")+(level(flags)>before?" Profession level "+level(flags)+"!":"")+discovery;
    }
    public String sell(int tier,Player p,Set<String> flags){CoopDungeon.require(tier>=0&&tier<3,"Choose a material.");int owned=number(flags,"item"+tier);CoopDungeon.require(owned>=5,"You need five materials to sell a bundle.");int value=10+10*tier;p.addCoins(value);put(flags,"item"+tier,owned-5);return "Sold five "+materials[tier]+" for "+value+" coins.";}
}
