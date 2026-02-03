package de.kseek.core.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import de.kseek.core.protostuff.PFMessage;
import de.kseek.core.protostuff.ProtostuffUtil;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class WebSocketMessageEncoder extends MessageToMessageEncoder<PFMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PFMessage msg, List<Object> out) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(ProtostuffUtil.serialize(msg));
        out.add(new BinaryWebSocketFrame(byteBuf));
//        int len = 0;
//        if (msg.data != null) {
//            len = msg.data.length;
//        }
//        ByteBuf byteBuf = Unpooled.buffer(len + 20);
//        byteBuf.writeShort(msg.messageType);
//        byteBuf.writeShort(msg.cmd);
//        byteBuf.writeBytes(msg.data);
//        byteBuf.writeLong(msg.msgId);
//        byteBuf.writeLong(msg.resultCode);
//        out.add(new BinaryWebSocketFrame(byteBuf));
    }
}
