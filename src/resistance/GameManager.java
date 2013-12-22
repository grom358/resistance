package resistance;

import io.netty.channel.Channel;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class GameManager {

    private static final GameManager SINGLETON = new GameManager();
    private int sessionId = 0;
    private GameSession gameSession;
    private int playerIndex;

    public static GameManager getInstance() {
        return SINGLETON;
    }

    private GameManager() {
        startSession();
    }

    public ConnectionDetails getConnection(NettyServerHandler handler, Channel channel, String botName) {
        gameSession.players[playerIndex].handler = handler;
        gameSession.players[playerIndex].channel = channel;
        gameSession.players[playerIndex].name = botName;
        playerIndex++;
        if (playerIndex == gameSession.players.length) {
            new Thread(gameSession).start();
            startSession();
        }
        return (new ConnectionDetails(gameSession, playerIndex));
    }

    private void startSession() {
        PlayerSession[] playerSessions = new PlayerSession[5];
        for (int i = 0; i < playerSessions.length; i++) {
            playerSessions[i] = new PlayerSession(i + "");
        }
        gameSession = new GameSession(sessionId, playerSessions);
        sessionId++;
        playerIndex = 0;
    }

}
