import java.util.*;
/** Earned only after completing a regional boss route, including its mission objectives. */
public final class BossMaterials {
    private BossMaterials() {}
    public static String name(int region) { return List.of("Grove heartwood", "Chieftain's iron", "Archive sigil", "Ashwing ember").get(region); }
    private static String prefix(int region) { return "boss:material:"+region+":"; }
    public static int owned(Set<String> flags,int region) { return flags.stream().filter(f->f.startsWith(prefix(region))).mapToInt(f->Integer.parseInt(f.substring(prefix(region).length()))).max().orElse(0); }
    private static void set(Set<String> flags,int region,int value) { flags.removeIf(f->f.startsWith(prefix(region)));flags.add(prefix(region)+value); }
    public static String award(Set<String> flags,int route) {
        if(route%3!=2)return "";
        int region=route/3;set(flags,region,Math.min(999,owned(flags,region)+1));return name(region);
    }
    public static void consume(Set<String> flags,int region,int amount) { CoopDungeon.require(owned(flags,region)>=amount,"Not enough "+name(region)+".");set(flags,region,owned(flags,region)-amount); }
}
