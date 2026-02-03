package de.kseek.core.ws;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class WebSocketChildChannelHandler extends ChannelInitializer<SocketChannel> {

    private int timeOutSecond = 150;

    public WebSocketChildChannelHandler() {
    }

    public WebSocketChildChannelHandler(int timeOutSecond) {
        this.timeOutSecond = timeOutSecond;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        // TODO Auto-generated method stub
        ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(timeOutSecond, timeOutSecond, timeOutSecond, TimeUnit.SECONDS));
        ch.pipeline().addLast("http-codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65535));
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast("encoder", new WebSocketMessageEncoder());
        ch.pipeline().addLast("handler", new WebSocketServerHandler());
    }
}
