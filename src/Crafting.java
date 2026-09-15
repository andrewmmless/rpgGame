import java.util.*;
/** Deterministic recipes: no rerolling, no legendary shortcut. */
public final class Crafting {
    public record Ingredient(Gathering profession,int tier,int amount) {}
    public record Recipe(String id,String name,int route,int level,int coins,Equipment.Slot slot,Equipment.Rarity rarity,WeaponAttribute attribute,List<Ingredient> ingredients) {}
    public static final List<Recipe> RECIPES=List.of(
        new Recipe("trail_coat","Reinforced trail coat",0,5,12,Equipment.Slot.ARMOUR,Equipment.Rarity.COMMON,WeaponAttribute.NONE,List.of(new Ingredient(Gathering.FORAGING,0,8))),
        new Recipe("iron_weapon","Ironwood weapon",4,25,70,Equipment.Slot.WEAPON,Equipment.Rarity.RARE,WeaponAttribute.PIERCING,List.of(new Ingredient(Gathering.FORAGING,1,10),new Ingredient(Gathering.MINING,1,12))),
        new Recipe("ward_weapon","Wardbound weapon",8,45,200,Equipment.Slot.WEAPON,Equipment.Rarity.EPIC,WeaponAttribute.FOCUS,List.of(new Ingredient(Gathering.FORAGING,2,15),new Ingredient(Gathering.MINING,2,18),new Ingredient(Gathering.FISHING,2,8))),
        new Recipe("conduit_weapon","Conduit weapon",8,45,200,Equipment.Slot.WEAPON,Equipment.Rarity.EPIC,WeaponAttribute.CONDUIT,List.of(new Ingredient(Gathering.FORAGING,2,15),new Ingredient(Gathering.MINING,2,18),new Ingredient(Gathering.FISHING,1,15))),
        new Recipe("bloodsteel_weapon","Bloodsteel weapon",5,30,100,Equipment.Slot.WEAPON,Equipment.Rarity.RARE,WeaponAttribute.VAMPIRIC,List.of(new Ingredient(Gathering.FORAGING,1,15),new Ingredient(Gathering.MINING,1,20))),
        new Recipe("field_potions","Two field potions",6,1,4,null,null,null,List.of(new Ingredient(Gathering.FISHING,0,3),new Ingredient(Gathering.FORAGING,0,2)))
    );
    public static Recipe recipe(String id){return RECIPES.stream().filter(r->r.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown recipe."));}
    public static List<Map<String,Object>> view(Player p,Set<String> flags,Set<String> cleared){return RECIPES.stream().map(r->{Map<String,Object> out=new LinkedHashMap<>();boolean unlocked=SubArea.get(r.route()).unlocked(flags,cleared)&&p.getLevel()>=r.level();out.put("id",r.id());out.put("name",r.name());out.put("level",r.level());out.put("coins",r.coins());out.put("route",SubArea.get(r.route()).name());out.put("unlocked",unlocked);out.put("ready",unlocked&&p.getCoins()>=r.coins()&&r.ingredients().stream().allMatch(i->i.profession().owned(flags,i.tier())>=i.amount()));out.put("result",r.slot()==null?"2 health potions":r.rarity()+" · +"+(2+r.level()/2+r.rarity().ordinal()*3)+" "+(r.slot()==Equipment.Slot.WEAPON?"attack · "+r.attribute():"armour"));out.put("ingredients",r.ingredients().stream().map(i->Map.of("name",i.profession().materials[i.tier()],"amount",i.amount(),"owned",i.profession().owned(flags,i.tier()))).toList());return out;}).toList();}
    public static Equipment craft(String id,Player p,Set<String> flags,Set<String> cleared,int inventorySize){
        Recipe r=recipe(id);CoopDungeon.require(SubArea.get(r.route()).unlocked(flags,cleared)&&p.getLevel()>=r.level(),"Reach the recipe level and open its story route first.");
        CoopDungeon.require(r.slot()==null?p.getPotions()<=97:inventorySize<30,r.slot()==null?"Make room in your potion pouch.":"Make room in your bag.");
        CoopDungeon.require(p.getCoins()>=r.coins(),"Not enough coins.");
        for(Ingredient i:r.ingredients())CoopDungeon.require(i.profession().owned(flags,i.tier())>=i.amount(),"Not enough "+i.profession().materials[i.tier()]+".");
        p.spendCoins(r.coins());for(Ingredient i:r.ingredients())i.profession().consume(flags,i.tier(),i.amount());
        if(r.slot()==null){p.addPotion();p.addPotion();return null;}
        String name=r.slot()==Equipment.Slot.WEAPON?r.name().replace("weapon",Equipment.weaponName(p.getPlayerClass(),r.rarity())):r.name();
        return new Equipment(UUID.randomUUID().toString(),name,r.slot(),r.rarity(),r.level(),2+r.level()/2+r.rarity().ordinal()*3,0,r.attribute());
    }
}
