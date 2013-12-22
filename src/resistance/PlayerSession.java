package resistance;

import io.netty.channel.Channel;

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
    public Channel channel;
    public NettyServerHandler handler;

    public PlayerSession(String name) {
        this.name = name;
    }

    public void sendMessage(String msg) {
        if (channel.isActive()) {
            channel.writeAndFlush(msg + "\r\n");
        }
    }
}
