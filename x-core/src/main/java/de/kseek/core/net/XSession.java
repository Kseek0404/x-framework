package de.kseek.core.net;

import com.google.protobuf.GeneratedMessage;
import de.kseek.core.protostuff.PFMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class XSession extends Session<GeneratedMessage, PFMessage> implements Inbox<PFMessage>{
    public XSession(String id, NetAddress netAddress, Connect connect) {
        super(id, connect, netAddress);
    }

    @Override
    public void send(GeneratedMessage gmsg) {

    }

    @Override
    public void onClusterReceive(Connect connect, PFMessage message) {
    }
}
