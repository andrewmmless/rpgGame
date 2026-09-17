/** Predictable boss cycles. Plans are locked before player actions so warnings remain truthful. */
public final class BossPattern {
    private BossPattern() {}
    public static boolean regional(String name){return name.equals("Ironbound Chieftain")||name.equals("Lantern Lich")||name.contains("Alpha Wolf")||name.contains("Orc Chieftain")||name.contains("Lich Acolyte")||name.contains("Ashwing");}
    public static Enemy.Move move(String name,int round,boolean desperate){
        Enemy.Move special=name.contains("Wolf")?Enemy.Move.POISON:name.contains("Chieftain")?Enemy.Move.GUARD:name.contains("Lich")?Enemy.Move.RECOVER:Enemy.Move.SAP;
        return switch(Math.floorMod(round,4)){case 0->special;case 1->Enemy.Move.CHARGE;case 2->Enemy.Move.HEAVY;default->desperate?Enemy.Move.DRAIN:Enemy.Move.ATTACK;};
    }
}
