package de.kseek.core.constant;

/**
 * 全架构 messageType / cmd 编号规划与常量。
 *
 * <h2>messageType 区间规划</h2>
 * <ul>
 *   <li><b>1–99：框架保留</b>
 *     <ul>
 *       <li>1：心跳 (Ping/Pong)</li>
 *       <li>2–49：预留（鉴权、压缩、加密等框架级扩展）</li>
 *       <li>50–99：预留</li>
 *     </ul>
 *   </li>
 *   <li><b>100–199：会话/集群/网关内部</b>
 *     <ul>
 *       <li>100：会话与集群消息 ({@link SessionConst})</li>
 *       <li>150：下发给客户端的系统消息 ({@link ToClientConst})</li>
 *       <li>其余 101–149、151–199 预留</li>
 *     </ul>
 *   </li>
 *   <li><b>1000+：业务消息</b>
 *     <ul>
 *       <li>由各业务模块自行规划，建议按千位分段，如 1000=登录、2000=大厅、3000=游戏等</li>
 *       <li>同一 messageType 下 cmd 从 1 起连续编号，请求与响应可用奇偶或 req/resp 区分</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>cmd 规划（同一 messageType 内）</h2>
 * <ul>
 *   <li>框架内：从 1 开始连续编号（如心跳 Ping=1, Pong=2）</li>
 *   <li>会话/集群：见 {@link SessionConst}，从 101 起编号以保持历史兼容</li>
 *   <li>业务：建议从 1 开始，请求/响应成对（如 1=Req, 2=Resp）</li>
 * </ul>
 *
 * <h2>防止 messageType / cmd 重复</h2>
 * <ul>
 *   <li><b>全工程唯一</b>：(messageType, cmd) 二元组在框架+所有业务模块中必须唯一，否则路由会错乱。</li>
 *   <li><b>框架区</b>：1–99、100–199 仅在本类及框架内使用，业务禁止占用。</li>
 *   <li><b>业务区</b>：1000+ 按千位分段给各模块（如 1000=登录、2000=大厅、3000=游戏），每模块只在自己的段内定义 messageType；同一 messageType 下 cmd 从 1 起连续编号。</li>
 *   <li><b>常量集中</b>：业务建议使用统一的常量类（如 game-common 的 GameMessageConst）或每模块一个 Const，避免魔法数字；新增号必须在常量中登记。</li>
 *   <li><b>启动校验</b>：{@link de.kseek.core.protostuff.MessageUtil#validateMessageTypeCmdUnique} 会在加载消息时校验 (messageType, cmd) 唯一，重复则启动抛异常。</li>
 * </ul>
 *
 * @author kseek
 * @date 2024/3/22
 */
public interface MessageConst {

    /**
     * 框架级：心跳消息 (Ping/Pong)。
     * messageType=1, cmd: 1=Ping(请求), 2=Pong(响应)。
     */
    interface HeartbeatConst {
        int TYPE = 1;
        int CMD_PING = 1;
        int CMD_PONG = 2;
    }

    /**
     * 会话与集群内部消息。
     * messageType=100，cmd 见下方常量。
     */
    interface SessionConst {
        int TYPE = 100;
        int NOTIFY_SESSION_QUIT = 101;
        int NOTIFY_SESSION_VERIFY_PASS = 102;
        int NOTIFY_SESSION_ENTER = 103;
        int NOTIFY_SWITCH_NODE = 104;
        int NOTIFY_SESSION_LOGOUT = 105;
        int NOTIFY_SESSION_KICK_OUT = 106;
        int CLUSTER_CONNECT_REGISTER = 107;
        //广播消息
        int BROADCAST_MSG = 108;
    }

    /**
     * 下发给客户端的系统消息（如服务器状态）。
     * messageType=150，cmd 见下方常量。
     */
    interface ToClientConst {
        int TYPE = 150;
        int REQ_SERVER_STATUS = 151;
        int RESP_SERVER_STATUS = 152;
    }
}
