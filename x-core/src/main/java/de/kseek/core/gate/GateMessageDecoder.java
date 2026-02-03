package de.kseek.core.gate;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import de.kseek.core.protostuff.PFMessage;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class GateMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // copy the ByteBuf content to a byte array
        int messageType = msg.readUnsignedShort();
        int cmd = msg.readUnsignedShort();
        byte[] array = new byte[msg.readableBytes()];
        msg.getBytes(msg.readerIndex(), array, 0, array.length);
        PFMessage message = new PFMessage(messageType, cmd, array);
        out.add(message);
    }
}
