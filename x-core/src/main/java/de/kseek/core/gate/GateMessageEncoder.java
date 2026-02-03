package de.kseek.core.gate;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import de.kseek.core.protostuff.PFMessage;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class GateMessageEncoder extends MessageToMessageEncoder<PFMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PFMessage msg, List<Object> out) throws Exception {
        int len = 0;
        if (msg.data != null) {
            len = msg.data.length;
        }
        ByteBuf byteBuf = Unpooled.buffer(len + 4);
        byteBuf.writeShort(msg.messageType);
        byteBuf.writeShort(msg.cmd);
        byteBuf.writeBytes(msg.data);
        out.add(byteBuf);
    }
}
