package de.kseek.core.netty.codec;

import de.kseek.core.protostuff.PFMessage;
import de.kseek.core.protostuff.ProtostuffUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class PFMessageEncoder extends MessageToMessageEncoder<PFMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PFMessage msg, List<Object> out) throws Exception {
        out.add(Unpooled.wrappedBuffer(ProtostuffUtil.serialize(msg)));
    }
}
