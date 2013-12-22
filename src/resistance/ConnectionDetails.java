package resistance;

/**
 *
 * @author Andrew
 */
public class ConnectionDetails {

    public GameSession gameSession;
    public int playerIndex;

    public ConnectionDetails(GameSession gameSession, int playerIndex) {
      this.gameSession = gameSession;
      this.playerIndex = playerIndex;
    }
}
