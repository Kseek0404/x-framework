package de.kseek.core.gate;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class GateChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final int MSG_MAX_SIZE = 10 * 1024 * 1024;

    private static final int HAND_SIZE = 4;

    private int timeOutSecond = 30;

    public GateChannelInitializer() {
    }

    public GateChannelInitializer(int timeOutSecond) {
        if (timeOutSecond > 0) {
            this.timeOutSecond = timeOutSecond;
        }
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                        MSG_MAX_SIZE, 0, HAND_SIZE, 0, 4))
                .addLast(new GateMessageDecoder())
                .addLast(new LengthFieldPrepender(HAND_SIZE))
                .addLast(new GateMessageEncoder())
                .addLast("idleStateHandler", new IdleStateHandler(timeOutSecond, 0, 0, TimeUnit.SECONDS))
//                .addLast("idleStateHandler", new IdleStateHandler(25, 25, 20, TimeUnit.SECONDS))
//                .addLast(new ChannelDuplexHandler() {
//                    @Override
//                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//                        if (evt instanceof IdleStateEvent) {
//                            IdleStateEvent e = (IdleStateEvent) evt;
//                            if (e.state() == IdleState.ALL_IDLE) {
//                                ctx.close();
//                            }
//                        }
//                    }
//                })
                .addLast(new GateSession());
    }
}
