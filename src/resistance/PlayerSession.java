package resistance;

/**
 *
 * @author grom
 */
public class PlayerSession {
    public String name;
    public boolean isSpy;
    public volatile boolean vote;
    public volatile boolean onMission;
    public volatile boolean missionVote;
    
    public PlayerSession(String name) {
        this.name = name;
    }
}
