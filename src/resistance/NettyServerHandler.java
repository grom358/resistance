package resistance;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author grom
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private static final int BOT_NAME_PREFIX = "botname ".length();
    private static final Logger logger = Logger.getLogger(
            NettyServerHandler.class.getName());
    private GameSession gameSession;
    private int playerIndex;
    protected ClientState clientState;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientState = ClientState.CONNECTED;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        switch (clientState) {
            case CONNECTED:
                if (msg.startsWith("botname ") && msg.length() > BOT_NAME_PREFIX) {
                    //TODO: DEAL WITH DUPLICATE BOTNAMES
                    String botName = msg.substring(BOT_NAME_PREFIX);
                    ConnectionDetails c = GameManager.getInstance().getConnection(this, ctx.channel(), botName);
                    gameSession = c.gameSession;
                    playerIndex = c.playerIndex;
                    clientState = ClientState.IDLE;
                }
                break;
            case LEADER:
                String botNames[] = msg.split(" ");
                //boolean valid = false;
                if (botNames.length == gameSession.getTeamSize()) {
                    for (String botName : botNames) {
                        if (!gameSession.checkName(botName)) {
                            break;
                        }
                    }
                    gameSession.onTeamSelection(playerIndex, botNames);
                    clientState = ClientState.IDLE;
                }

                break;
            case VOTE:
                if (msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("no")) {
                    gameSession.onVote(playerIndex, msg.equalsIgnoreCase("yes"));
                    clientState = ClientState.IDLE;
                }
                break;
            case MISSION_VOTE:
                if (msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("no")) {
                    gameSession.onMissionVote(playerIndex, msg.equalsIgnoreCase("yes"));
                    clientState = ClientState.IDLE;
                }
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }
}
