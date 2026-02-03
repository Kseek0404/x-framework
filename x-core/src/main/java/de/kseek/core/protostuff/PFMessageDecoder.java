package de.kseek.core.protostuff;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class PFMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // copy the ByteBuf content to a byte array
        byte[] array = new byte[msg.readableBytes()];
        msg.getBytes(0, array);
        out.add(ProtostuffUtil.deserialize(array, PFMessage.class));
    }
}
