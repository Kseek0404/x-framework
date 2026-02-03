package de.kseek.core.ws;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import de.kseek.core.gate.GateSession;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class WSGateSession extends GateSession {

    @Override
    protected void checkServer() {
        AttributeKey<String> key = AttributeKey.valueOf("X-Real_IP");
        if (channel.hasAttr(key)) {
            Attribute<String> attribute = channel.attr(key);
            if (attribute != null && attribute.get() != null) {
                remoteAddress.setHost(attribute.get());
            }
        }
        super.checkServer();
    }

//    @Override
//    public boolean write(Object message) {
//        ctx.writeAndFlush(createMsg(message)).addListener(future -> {
//            if (!future.isSuccess()) {
//                Throwable e = future.cause();
//                if (e != null) {
//                    log.warn("消息写出异常,netAddress=" + remoteAddress + ",ctx=" + ctx, e);
//                }
//            }
//        });
//        return true;
//    }
//
//    @Override
//    public void writeAndClose(Object message) {
//        log.debug("服务器主动关闭连接并通知,netAddress={},ctx={}", remoteAddress, ctx);
//        ctx.writeAndFlush(createMsg(message)).addListener(future -> {
//            if (!future.isSuccess()) {
//                Throwable e = future.cause();
//                if (e != null) {
//                    log.warn("消息写出异常,netAddress=" + remoteAddress + ",ctx=" + ctx, e);
//                }
//            }
//            if (isActive()) {
//                ctx.close();
//                log.debug("服务器主动关闭连接并通知完成,netAddress={},ctx={}", remoteAddress, ctx);
//            }
//        });
//    }
//
//    public BinaryWebSocketFrame createMsg(Object message) {
//        PFMessage msg = (PFMessage) message;
//        int len = 0;
//        if (msg.data != null) {
//            len = msg.data.length;
//        }
//        ByteBuf byteBuf = Unpooled.buffer(len + 4);
//        byteBuf.writeShort(msg.messageType);
//        byteBuf.writeShort(msg.cmd);
//        byteBuf.writeBytes(msg.data);
//
//        return new BinaryWebSocketFrame(byteBuf);
//    }
}
