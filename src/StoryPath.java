import java.util.*;

/** Small persistent story layer. Flags live in the existing saved milestone set. */
public final class StoryPath {
    private StoryPath() {}
    public static final String BRIEF="story:muster",ACCEPT="story:eastern_road",REPORT="story:road_reported";
    public static Map<String,Object> view(Set<String> flags,Set<String> cleared) {
        boolean brief=flags.contains(BRIEF),accepted=flags.contains(ACCEPT),reported=flags.contains(REPORT),won=cleared.contains(Area.WHISPERING_WOODS.name());
        String title,words,button,action;
        if(reported){title="The missing ward";words="Elin studies the broken royal seal. ‘Wolves did not take this ward apart. The road to Stonefang is open; find out what the miners saw.’ Your next route is Stonefang Caves.";button="Recall the report";action="recap";}
        else if(accepted){title="Secure the eastern road";words=won?"The Alpha Wolf is defeated. Captain Elin is waiting for your report on the reopened road.":"‘The wagons carry food, not treasure,’ Elin says. ‘Follow the eastern road through Whispering Woods and stop the pack at its source. Return after defeating the Alpha Wolf.’";button=won?"Report the Woods victory":"Review your orders";action=won?"report":"recap";}
        else if(brief){title="A place in crown service";words="‘A seal does not keep a family fed,’ Captain Elin says. ‘Safe roads do. Help me reopen this one.’ Tessa Reed tightens her courier's satchel. ‘Mara is already stretching the last flour.’ Across the muster yard, Cedric Ashford looks away. ‘Surely the crown has grander work.’";button="Accept the road commission";action="accept";}
        else {title="The Hearthglen muster";words="Relief wagons have stopped arriving from the eastern road. Captain Elin Ward is taking volunteers into Cindergard's frontier company. A courier waits beside her; a young noble watches from the steps.";button="Speak to Captain Elin";action="brief";}
        if(accepted && (!won||reported)) {
            SubArea next=SubArea.ALL.stream().filter(r->!r.complete(flags,cleared)).findFirst().orElse(null);
            if(next==null){title="For Cindergard";words=SubArea.get(11).ending();button="Recall your service";action="recap";}
            else {title=next.mission();words=next.opening()+" Mission: "+next.name()+" (recommended levels "+next.minLevel()+"–"+next.maxLevel()+").";button="Review your orders";action="recap";}
        }
        if(!StoryConsequences.choice(flags).isEmpty())words+=" Your company chose "+(StoryConsequences.choice(flags).equals("protect")?"a sealed inquiry: protect Cedric’s witness in the Drowned Archive.":"a public inquiry: secure the wards and expose the evidence.");
        return Map.of("title",title,"speaker","Captain Elin Ward · Crown frontier company","text",words,"button",button,"action",action,"accepted",accepted,"reported",reported);
    }
    public static String speak(String action,Set<String> flags,Set<String> cleared) {
        switch(action) {
            case "brief" -> flags.add(BRIEF);
            case "accept" -> {if(!flags.contains(BRIEF))throw new IllegalArgumentException("Speak to Captain Elin first.");flags.add(ACCEPT);}
            case "report" -> {if(!flags.contains(ACCEPT)||!cleared.contains(Area.WHISPERING_WOODS.name()))throw new IllegalArgumentException("Accept the commission and defeat the Alpha Wolf first.");flags.add(REPORT);}
            case "recap" -> { }
            default -> throw new IllegalArgumentException("Choose a dialogue option.");
        }
        return (String)view(flags,cleared).get("text");
    }
}
