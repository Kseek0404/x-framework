package de.kseek.core.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class TaskGroup implements Runnable{
    protected ExecutorService executor;
    protected final Queue<Runnable> queue = new LinkedBlockingQueue<>();
    protected volatile boolean inLoop = false;

    public TaskGroup(ExecutorService executor) {
        this.executor = executor;
    }

    private void fire() {
        if (!inLoop) {
            inLoop = true;
            executor.submit(this);
        }
    }

    @Override
    public void run() {
        Runnable r = queue.poll();
        if (r != null) {
            try {
                r.run();
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("task run error", e);
                }
            }
        }
        inLoop = false;
        if (!queue.isEmpty()) {
            fire();
        }
    }

    public void submit(Runnable runnable) {
        queue.offer(runnable);
        fire();
    }

    public void clean() {
        queue.clear();
    }
}
