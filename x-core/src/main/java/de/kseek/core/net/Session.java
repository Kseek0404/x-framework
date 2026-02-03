package de.kseek.core.net;

import lombok.Data;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Data
public abstract class Session<T, M> implements ConnectListener {
    protected String sessionId;
    protected Connect connect;
    protected NetAddress address;
    protected Object reference;
    protected SessionListener sessionListener;

    public Session() {
    }

    public Session(String sessionId, Connect connect, NetAddress address) {
        this.sessionId = sessionId;
        this.connect = connect;
        this.address = address;
    }

    public abstract void send(T msg);

    public void close() {
        if (connect != null && connect.isActive()) {
            connect.close();
        }
    }

    @Override
    public void onConnectClose(Connect connect) {
        if (sessionListener != null) {
            sessionListener.onSessionClose(this);
        }
    }

    public String sessionId() {
        return sessionId;
    }
}
