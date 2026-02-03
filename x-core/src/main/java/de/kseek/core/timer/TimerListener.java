
package de.kseek.core.timer;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface TimerListener {
    /**
     * 定时事件的监听方法
     */
    void onTimer(TimerEvent e);
}
