import java.util.*;
public final class BossCounters {
 private BossCounters(){}
 public static boolean interrupt(Enemy enemy,Enemy.Move planned,boolean damagingAbility,int damage,List<String> events){
  if(!enemy.isBoss())return false;
  if(enemy.getName().contains("Lich")&&planned==Enemy.Move.RECOVER&&damagingAbility){events.add("The damaging ability shatters the soul ritual. Healing interrupted.");return true;}
  if(enemy.getName().contains("Chieftain")&&planned==Enemy.Move.GUARD&&damage>0){events.add("Your strike cracks the Chieftain's iron stance before it forms.");return true;}
  return false;
 }
}
