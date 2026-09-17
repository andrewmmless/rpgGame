import java.util.*;

/** Presentation data derived from saved mission state; no extra save fields required. */
public final class SharedMissionScene {
    private SharedMissionScene() {}
    private static final String[][] TITLES={
        {"The wagons leave Hearthglen","A wheel in the mud","The last bend"},
        {"The silent watchpost","Marks beneath the claw scars","A witness in the ruins"},
        {"Cedric holds the road","The stripped ward","The pack's source"}
    };
    private static final String[][] SCENES={
        {"Tessa walks beside the flour wagon. ‘One of you keep the road clear. The other stays with me.’ Beyond the tree line, something follows the wheels.",
         "A wheel sinks beside a broken milestone. Cedric reaches for the reins, then looks to you. ‘Tell me where you need me.’ Keep the supplies moving while your partner holds the attackers.",
         "Hearthglen's lamps appear through the branches. The packleader bars the final bend. Tessa grips the reins: ‘We are not leaving the food here.’"},
        {"Three signal fires have gone dark. Tessa finds a watchman's scarf beside a splintered door. Search while your partner keeps the creatures away.",
         "Under the claw marks are clean cuts from royal tools. Cedric kneels beside the empty ward socket. ‘That was removed before the attack.’ Recover the evidence together.",
         "A survivor shelters beneath the fallen stairs. The watchhound circles the doorway. Hold its attention while your partner searches for the seal that explains the missing ward."},
        {"Cedric takes the rear without being asked. ‘Bring back the seal. I will keep the road open.’ Ahead, the stolen ward's magic has turned against the grove.",
         "Roots tighten around the exposed ward socket. Elin's order is clear: weaken the hostile magic before it claims another patrol. Disruption gives your partner room to strike.",
         "The Alpha Wolf waits beside the stripped ward. Beyond it lies the proof the court needs. Break the ward, watch the wolf's warning, and finish what began beside the flour wagon."}
    };
    public static Map<String,Object> view(CoopDungeon p){
        if(p.storyRoute==null)return Map.of();
        SubArea route=SubArea.get(p.storyRoute);int stage=Math.max(0,Math.min(2,p.stage));
        boolean firstChapter=route.index()<3;
        String title=firstChapter?TITLES[route.index()][stage]:new String[]{"Enter the mission site","Hold the route","Face the guardian"}[stage];
        String text=firstChapter?SCENES[route.index()][stage]:stage==0?route.opening():stage==1?"Your company regroups. The route is not yet secure; finish the work here before approaching the guardian.":route.guardian()+" holds the final passage. Complete your shared objective and defeat the guardian.";
        if(p.state.equals("LOBBY")){title=route.mission();text=route.opening();}
        if(p.state.equals("VICTORY")){title=route.index()==2?"Chapter one complete · A road reclaimed":"Mission complete · "+route.name();text=StoryConsequences.ending(route.index(),route.ending(),p.members.isEmpty()?Set.of():p.members.get(0).original.claimed());}
        String next=route.index()+1<SubArea.ALL.size()?"Next route: "+SubArea.get(route.index()+1).name()+". Return to the capital, rest and check your new equipment. Both players keep their own progress.":"Cindergard's campaign is complete. Return to the capital together; open routes remain available for patrols.";
        String advice=p.enemyHealth<=0?"The enemy is defeated. Finish the objective; your partner can help with another objective action.":switch(p.objective()){
            case ESCORT -> p.integrity<=40?"The convoy is in danger. Escort now to restore supplies; your partner can cover the attack.":"Split the work: one escorts while the other fights. Either player can change roles each round.";
            case INVESTIGATE -> "Searching leaves you exposed. One player searches while the other protects them or fights.";
            case SEAL -> "Disrupting the ward weakens this round's enemy attack. Coordinate a disruption with your partner's attack.";
        };
        if(p.objectiveProgress>=3&&p.enemyHealth>0)advice=p.objective()==MissionObjective.ESCORT?"Objective ready. Defeat the enemy, but keep escorting if supply integrity falls.":"Objective complete. Focus on the enemy and respond to its next attack.";
        return Map.of("title",title,"text",text,"speaker",route.captain(),"advice",advice,"next",next,"chapter",route.index()/3+1);
    }
}
