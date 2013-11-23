package resistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author grom
 */
public class GameSession implements Runnable {
    /**
     * Lookup table of number of spies needed for a game
     */
    static private final int[] SPY_TABLE = {2, 2, 3, 3, 3, 4};

    /**
     * Lookup table of number of players needed to go on a mission
     */
    static private final int[][] MISSION_REQUIREMENT = {
        {2, 3, 2, 3, 3},
        {2, 3, 4, 3, 4},
        {2, 3, 3, 4, 4},
        {3, 4, 4, 5, 5},
        {3, 4, 4, 5, 5},
        {3, 4, 4, 5, 5}
    };
    
    static private final Random random = new Random();
    
    static final int TIMELIMIT = 2000;
    
    private final int sessionId;
    private final PlayerSession[] players;
    private volatile boolean teamSelected;
    private volatile int leader;
    private int roundNo;
    private int spyWins;
    private boolean spyAltWin;
    private int rebelWins;
    private int teamSize;
    private final int minVote;
    private CountDownLatch latch;
    private int gamesLeft = 1;        
    
    public GameSession(int sessionId, PlayerSession[] players) {
        minVote = players.length / 2 + 1;
        this.sessionId = sessionId;
        this.players = players;
    }
    
    @Override
    public void run() {
        while (gamesLeft > 0) {
            playGame();
        }
        debug("Done");
    }
    
    private void debug(String msg) {
        System.out.println("#" + sessionId + ": " + msg);
    }
    
    private void playGame() {
        debug("Start Game");
        startGame();
        while (!spyAltWin && spyWins < 3 && rebelWins < 3) {            
            playRound();
        }
        debug("Score " + rebelWins + "-" + spyWins);
        gamesLeft--;        
    }
    
    static private List<Integer> randomPlayers(int count, int n) {
        ArrayList<Integer> al = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            al.add(i);
        }
        Collections.shuffle(al, random);
        return al.subList(0, n);
    }

    private void startGame() {
        roundNo = 0;
        spyWins = 0;
        rebelWins = 0;
        spyAltWin = false;
        // Randomly pick spies
        for (PlayerSession p : players) {
            p.isSpy = false;
        }
        int i = players.length - 5;
        int noSpies = SPY_TABLE[i];
        List<Integer> spies = randomPlayers(players.length, noSpies);
        debug("Spies - " + spies);
        for (int playerIndex : spies) {
            players[playerIndex].isSpy = true;
        }
        // Randomly select leader
        leader = random.nextInt(players.length + 1);    
    }
    
    private void playRound() {
        debug("Start Round " + (roundNo + 1));
        int i = players.length - 5;
        teamSize = MISSION_REQUIREMENT[i][roundNo];
        getTeam();
        boolean teamApproved = getVotes();
        int voteNo = 0;
        while (!teamApproved) {
            debug("Team Rejected!");
            voteNo++;
            if (voteNo == 5) {
                spyAltWin = true;
                return;
            }
            teamApproved = getVotes();
        }
        //debug("Team selected");
        int sabotageCount = getMissionVotes();
        debug("Mission - " + sabotageCount);
        if (sabotageCount > 0) {
            spyWins++;
            debug("Mission failed");
        } else {
            debug("Mission success");
            rebelWins++;
        }            
        // rotate leader clockwise
        leader = (leader + 1) % players.length;
        roundNo++;
    }
    
    private void getTeam() {
        debug("Leader picking team...");
        // Clear team selection
        for (PlayerSession p : players) {
            p.onMission = false;
        }
        teamSelected = false;
        latch = new CountDownLatch(1);
        // wait for leader to pick team
        try {
            latch.await(TIMELIMIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {            
        }
        if (!teamSelected) {            
            List<Integer> team = randomPlayers(players.length, teamSize);
            debug("FALLBACK; team is " + team);
            for (int playerIndex : team) {
                players[playerIndex].onMission = true;
            }
        }
        // TODO notify all the players of the team        
    }
    
    private boolean getVotes() {
        debug("Players voting...");
        // Setup default votes
        for (PlayerSession p : players) {
            p.vote = true;
        }
        latch = new CountDownLatch(players.length);
        // wait for players to submit votes
        try {
            latch.await(TIMELIMIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {            
        }
        int yesCount = 0;
        for (PlayerSession p : players) {
            if (p.vote) {
                yesCount++;
            }
        }
        // notify all the players of the vote results
        return yesCount >= minVote;
    }
    
    private int getMissionVotes() {
        debug("Players on mission...");
        // Setup default votes
        for (PlayerSession p : players) {
            p.missionVote = !p.isSpy;
        }
        latch = new CountDownLatch(teamSize);
        // wait for mission players to submit votes
        try {
            latch.await(TIMELIMIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {            
        }
        int sabotageCount = 0;
        for (PlayerSession p : players) {
            if (p.onMission && !p.missionVote) {
                sabotageCount++;
            }
        }
        // TODO notify players of mission votes
        return sabotageCount;
    }
    
    public void onTeamSelection(int playerIndex, int[] team) {
        if (playerIndex == leader) {
            boolean validSelection = team.length == teamSize;
            // TODO check for index duplicates and index are in range
            if (validSelection) {                
                for (int i : team) {
                    players[i].onMission = true;
                }
                teamSelected = true;
                latch.countDown();                
            } else {
                // TODO handle invalid team selection
            }
        } else {
            // TODO handle invalid leader from submitting team
        }
    }
    
    public void onVote(int playerIndex, boolean vote) {
        latch.countDown();
        players[playerIndex].vote = vote;
    }
    
    public void onMissionVote(int playerIndex, boolean vote) {
        if (players[playerIndex].onMission) {
            latch.countDown();
            players[playerIndex].missionVote = vote;
        } else {
            // TODO handle invalid player voting on mission
        }        
    }
}

