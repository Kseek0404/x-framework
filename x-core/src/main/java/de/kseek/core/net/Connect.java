package de.kseek.core.net;

/**
 * 网络连接抽象接口
 * @author kseek
 * @date 2024/3/22
 */
public interface Connect {
    /**
     * 向对端发送消息.
     *
     * @param msg
     * @return
     */
    boolean write(Object msg);
    /**
     * 主动关闭当前连接.
     *
     * @return
     */
    void close();
    /**
     * 检查当前连接是否处于活跃状态.
     *
     * @return
     */
    boolean isActive();
    /**
     * 获取连接的远程地址信息.
     *
     * @return
     */
    NetAddress address();
    /**
     * 生命周期回调：当连接被关闭时触发.
     */
    void onClose();
    /**
     * 生命周期回调：当连接建立成功时触发.
     */
    void onCreate();
    /**
     * 添加连接状态监听器.
     * @param connectListener
     */
    void addConnectListener(ConnectListener connectListener);
    /**
     * 移除连接状态监听器.
     * @param connectListener
     */
    void removeConnectListener(ConnectListener connectListener);
}
