package de.kseek.core.net;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface Inbox<T> {
    void onClusterReceive(Connect connect, T message);

}
