package resistance;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.Delimiters;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author grom
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = Logger.getLogger(
            NettyServerHandler.class.getName());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // Echo message back
        ctx.writeAndFlush(msg + "\r\n");

        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }
}
