import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Console persistence adapter. V4 saves are separate; legacy files are only read. */
public final class SaveManager {
    private static final Path SAVE_FOLDER=Path.of("saves-v4");
    private static Path path(Path folder,String name) {
        if(name==null || !name.matches("[\\p{L}\\p{N} _-]{1,60}"))throw new IllegalArgumentException("Use a name containing letters, numbers, spaces, _ or - (1–60 characters).");
        return folder.resolve(name+".txt");
    }
    public static void savePlayer(Player player) {
        try {
            Path file=path(SAVE_FOLDER,player.getName()); Files.createDirectories(SAVE_FOLDER);
            Properties data=new Properties();
            data.setProperty("Version","4"); data.setProperty("Name",player.getName());
            data.setProperty("Class",player.getPlayerClass().name());
            data.setProperty("Level",""+player.getLevel()); data.setProperty("Xp",""+player.getXp());
            data.setProperty("Health",""+player.getHealth());data.setProperty("Resource",""+player.getResource());
            data.setProperty("Coins",""+player.getCoins());data.setProperty("Potions",""+player.getPotions());
            data.setProperty("SwordDamage",""+player.getSwordDamage());
            Path temporary=Files.createTempFile(SAVE_FOLDER,"save-",".tmp");
            try {
                try(Writer writer=Files.newBufferedWriter(temporary)){data.store(writer,"RPG V4 foundation");}
                try { Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
                catch(AtomicMoveNotSupportedException e){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);}
            } finally {Files.deleteIfExists(temporary);}
            System.out.println("Game saved successfully!");
        } catch(IOException|IllegalArgumentException e){System.out.println("Could not save: "+e.getMessage());}
    }
    public static Player loadPlayer(String name) {
        try {
            Path file=path(SAVE_FOLDER,name);
            if(!Files.exists(file))file=path(Path.of("saves"),name);
            Properties data=new Properties();
            try(Reader reader=Files.newBufferedReader(file)){data.load(reader);}
            String version=data.getProperty("Version","3");
            if(!version.equals("3")&&!version.equals("4"))throw new IllegalArgumentException("Unsupported save version");
            String savedName=data.getProperty("Name",name);path(SAVE_FOLDER,savedName);
            Player player=PlayerClass.valueOf(data.getProperty("Class","").toUpperCase(Locale.ROOT)).create(savedName);
            player.setLevel(number(data,"Level",1));
            // Convert old fractional level progress, since V4 uses a different XP curve.
            int xp=number(data,"Xp",0);
            if(version.equals("3"))xp=(int)Math.min(Math.max(0,player.getXpToNextLevel()-1),(long)xp*player.getXpToNextLevel()/Math.max(1,number(data,"XpToNextLevel",20)));
            player.setXp(xp);
            int hp=number(data,"Health",player.getMaxHealth());
            if(version.equals("3"))hp=(int)Math.min(player.getMaxHealth(),(long)hp*player.getMaxHealth()/Math.max(1,number(data,"MaxHealth",player.getMaxHealth())));
            player.setHealth(hp);player.setResource(number(data,"Resource",player.getMaxResource()));
            player.setCoins(number(data,"Coins",0));player.setPotions(number(data,"Potions",3));player.setSwordDamage(number(data,"SwordDamage",0));
            System.out.println("Game loaded successfully!");return player;
        } catch(IOException|IllegalArgumentException e){System.out.println("Could not load: "+e.getMessage());return null;}
    }
    private static int number(Properties data,String key,int fallback){int value=Integer.parseInt(data.getProperty(key,""+fallback));if(value<0)throw new IllegalArgumentException("Negative "+key);return value;}
    public static void deleteSave(String name) {
        try {System.out.println(Files.deleteIfExists(path(SAVE_FOLDER,name))?"V4 save deleted.":"No V4 save found.");}
        catch(IOException|IllegalArgumentException e){System.out.println("Could not delete save: "+e.getMessage());}
    }
}
