package de.kseek.core.cluster;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import de.kseek.core.protostuff.ProtostuffUtil;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class ClusterMessageEncoder extends MessageToMessageEncoder<ClusterMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ClusterMessage msg, List<Object> out) throws Exception {
        out.add(Unpooled.wrappedBuffer(ProtostuffUtil.serialize(msg)));
    }
}
