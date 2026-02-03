package de.kseek.core.constant;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface MessageConst {
    interface SessionConst {
        int TYPE = 100;
        int NOTIFY_SESSION_QUIT = 101;
        int NOTIFY_SESSION_VERIFY_PASS = 102;
        //int NOTIFY_SESSION_CREATE = 103;
        int NOTIFY_SESSION_ENTER = 104;
        int NOTIFY_SWITCH_NODE = 105;
        int NOTIFY_SESSION_LOGOUT = 106;
        int NOTIFY_SESSION_KICK_OUT = 107;
        int CLUSTER_CONNECT_REGISTER = 108;
        //广播消息
        int BROADCAST_MSG = 109;
    }

    interface ToClientConst {
        int TYPE = 150;
        int REQ_SERVER_STATUS = 151;
        int RESP_SERVER_STATUS = 152;
    }
}
