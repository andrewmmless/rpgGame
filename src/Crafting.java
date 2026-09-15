import java.util.*;
/** Deterministic equipment goals backed by gathered and boss materials. */
public final class Crafting {
    public record Ingredient(Gathering profession,int tier,int amount) {}
    public record Recipe(String id,String name,int route,int level,int coins,Equipment.Slot slot,Equipment.Rarity rarity,WeaponAttribute attribute,List<Ingredient> ingredients,List<BossIngredient> bossIngredients) {
        public Recipe(String id,String name,int route,int level,int coins,Equipment.Slot slot,Equipment.Rarity rarity,WeaponAttribute attribute,List<Ingredient> ingredients) { this(id,name,route,level,coins,slot,rarity,attribute,ingredients,List.of()); }
    }
    public record BossIngredient(int region,int amount) {}
    private static Recipe trophy(String id,String name,int route,int level,Equipment.Slot slot,Equipment.Rarity rarity,WeaponAttribute attribute) {
        return new Recipe(id,name,route,level,level*3,slot,rarity,attribute,List.of(),List.of(new BossIngredient(route/3,3)));
    }
    public static final List<Recipe> RECIPES=List.of(
        trophy("grove_weapon","Heartwood weapon",2,15,Equipment.Slot.WEAPON,Equipment.Rarity.RARE,WeaponAttribute.SIPHON),
        trophy("chieftain_armour","Chieftain's warplate",5,30,Equipment.Slot.ARMOUR,Equipment.Rarity.RARE,WeaponAttribute.NONE),
        trophy("archive_weapon","Archive weapon",8,45,Equipment.Slot.WEAPON,Equipment.Rarity.EPIC,WeaponAttribute.CONDUIT),
        trophy("ashwing_armour","Ashwing's mantle",11,60,Equipment.Slot.ARMOUR,Equipment.Rarity.EPIC,WeaponAttribute.NONE),
        new Recipe("crown_weapon","Cindergard weapon",11,60,350,Equipment.Slot.WEAPON,Equipment.Rarity.LEGENDARY,WeaponAttribute.FOCUS,List.of(new Ingredient(Gathering.MINING,2,20),new Ingredient(Gathering.FORAGING,2,20)),List.of(new BossIngredient(0,2),new BossIngredient(1,2),new BossIngredient(2,2),new BossIngredient(3,3))),
        new Recipe("crown_armour","Crownfire plate",11,60,350,Equipment.Slot.ARMOUR,Equipment.Rarity.LEGENDARY,WeaponAttribute.NONE,List.of(new Ingredient(Gathering.MINING,2,20),new Ingredient(Gathering.FISHING,2,20)),List.of(new BossIngredient(0,2),new BossIngredient(1,2),new BossIngredient(2,2),new BossIngredient(3,3))),
        new Recipe("trail_coat","Reinforced trail coat",0,5,12,Equipment.Slot.ARMOUR,Equipment.Rarity.COMMON,WeaponAttribute.NONE,List.of(new Ingredient(Gathering.FORAGING,0,8))),
        new Recipe("iron_weapon","Ironwood weapon",4,25,70,Equipment.Slot.WEAPON,Equipment.Rarity.RARE,WeaponAttribute.PIERCING,List.of(new Ingredient(Gathering.FORAGING,1,10),new Ingredient(Gathering.MINING,1,12))),
        new Recipe("ward_weapon","Wardbound weapon",8,45,200,Equipment.Slot.WEAPON,Equipment.Rarity.EPIC,WeaponAttribute.FOCUS,List.of(new Ingredient(Gathering.FORAGING,2,15),new Ingredient(Gathering.MINING,2,18),new Ingredient(Gathering.FISHING,2,8))),
        new Recipe("conduit_weapon","Conduit weapon",8,45,200,Equipment.Slot.WEAPON,Equipment.Rarity.EPIC,WeaponAttribute.CONDUIT,List.of(new Ingredient(Gathering.FORAGING,2,15),new Ingredient(Gathering.MINING,2,18),new Ingredient(Gathering.FISHING,1,15))),
        new Recipe("bloodsteel_weapon","Bloodsteel weapon",5,30,100,Equipment.Slot.WEAPON,Equipment.Rarity.RARE,WeaponAttribute.VAMPIRIC,List.of(new Ingredient(Gathering.FORAGING,1,15),new Ingredient(Gathering.MINING,1,20))),
        new Recipe("field_potions","Two field potions",6,1,4,null,null,null,List.of(new Ingredient(Gathering.FISHING,0,3),new Ingredient(Gathering.FORAGING,0,2)))
    );
    public static Recipe recipe(String id){return RECIPES.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown recipe."));}
    public static List<Map<String,Object>> view(Player p,Set<String> flags,Set<String> cleared){return RECIPES.stream().map(r->{Map<String,Object> out=new LinkedHashMap<>();boolean unlocked=unlocked(r,p,flags,cleared);out.put("id",r.id());out.put("name",r.name());out.put("level",r.level());out.put("slot",r.slot()==null?"SUPPLIES":r.slot().name());out.put("rarity",r.rarity()==null?"COMMON":r.rarity().name());out.put("attributeDescription",r.attribute()==null?"":r.attribute().description());out.put("coins",r.coins());out.put("route",SubArea.get(r.route()).name());out.put("unlocked",unlocked);out.put("ready",unlocked&&p.getCoins()>=r.coins()&&r.ingredients().stream().allMatch(i->i.profession().owned(flags,i.tier())>=i.amount())&&r.bossIngredients().stream().allMatch(i->BossMaterials.owned(flags,i.region())>=i.amount()));out.put("result",r.slot()==null?"2 health potions":r.rarity()+" · +"+(2+r.level()/2+r.rarity().ordinal()*3)+" "+(r.slot()==Equipment.Slot.WEAPON?"attack · "+r.attribute():"armour"));List<Map<String,Object>> materials=new ArrayList<>();for(Ingredient i:r.ingredients())materials.add(Map.of("name",i.profession().materials[i.tier()],"amount",i.amount(),"owned",i.profession().owned(flags,i.tier())));for(BossIngredient i:r.bossIngredients())materials.add(Map.of("name",BossMaterials.name(i.region())+" · "+SubArea.get(i.region()*3+2).name(),"amount",i.amount(),"owned",BossMaterials.owned(flags,i.region())));out.put("ingredients",materials);out.put("bossRecipe",!r.bossIngredients().isEmpty());return out;}).toList();}
    private static boolean unlocked(Recipe r,Player p,Set<String> flags,Set<String> cleared) { return p.getLevel()>=r.level() && (r.bossIngredients().isEmpty()?SubArea.get(r.route()).unlocked(flags,cleared):SubArea.get(r.route()).complete(flags,cleared)); }
    public static Equipment craft(String id,Player p,Set<String> flags,Set<String> cleared,int inventorySize){
        Recipe r=recipe(id);CoopDungeon.require(unlocked(r,p,flags,cleared),"Reach the recipe level and unlock its route; boss recipes require completing that route.");
        CoopDungeon.require(r.slot()==null?p.getPotions()<=97:inventorySize<30,r.slot()==null?"Make room in your potion pouch.":"Make room in your bag.");
        CoopDungeon.require(p.getCoins()>=r.coins(),"Not enough coins.");
        for(Ingredient i:r.ingredients())CoopDungeon.require(i.profession().owned(flags,i.tier())>=i.amount(),"Not enough "+i.profession().materials[i.tier()]+".");
        for(BossIngredient i:r.bossIngredients())CoopDungeon.require(BossMaterials.owned(flags,i.region())>=i.amount(),"Not enough "+BossMaterials.name(i.region())+".");
        for(BossIngredient i:r.bossIngredients())BossMaterials.consume(flags,i.region(),i.amount());
        p.spendCoins(r.coins());for(Ingredient i:r.ingredients())i.profession().consume(flags,i.tier(),i.amount());
        if(r.slot()==null){p.addPotion();p.addPotion();return null;}
        String name=r.slot()==Equipment.Slot.WEAPON?r.name().replace("weapon",Equipment.weaponName(p.getPlayerClass(),r.rarity())):r.name();
        return new Equipment(UUID.randomUUID().toString(),name,r.slot(),r.rarity(),r.level(),2+r.level()/2+r.rarity().ordinal()*3,0,r.attribute());
    }
}
