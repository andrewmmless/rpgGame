/** Shared mission rules. Working the objective costs a combat turn. */
public enum MissionObjective {
    ESCORT("Keep the convoy moving","Escort supplies","Escort three times per stage. Each escort restores 15 integrity; unescorted rounds cost 10. You can keep escorting after three actions."),
    INVESTIGATE("Recover the evidence","Search the site","Search three times per stage while your partner holds the enemy. Searches leave you exposed."),
    SEAL("Break the hostile ward","Disrupt the ward","Disrupt three times per stage. Each disruption weakens the enemy's next attack.");
    public final String title,action,help;
    MissionObjective(String title,String action,String help){this.title=title;this.action=action;this.help=help;}
    public static MissionObjective forRoute(int index){return switch(index){case 0,3,6,9,10->ESCORT;case 1,4,7->INVESTIGATE;default->SEAL;};}
}
