package de.kseek.core.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.kseek.core.config.NodeConfig;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
@Order(9999)
@RequiredArgsConstructor
public class XWorkExecutor implements ApplicationRunner {
    /**
     * 本节点的配置信息
     */
    private final NodeConfig localNodeConfig;
    private final Map<Integer, TaskGroup> executorServiceMap = new ConcurrentHashMap<>();
    private ExecutorService threadPoolExecutor;

    public void submit(long id, Runnable work) {
        getTaskGroup(id).submit(work);
    }

    public void submit(TaskGroup taskGroup, Runnable work) {
        if (taskGroup != null) {
            taskGroup.submit(work);
        }
    }

    public TaskGroup newTaskGroup() {
        return new TaskGroup(threadPoolExecutor);
    }

    private TaskGroup getTaskGroup(long id) {
        int key = (int) (id % localNodeConfig.getWorkPoolNum());
        return executorServiceMap.computeIfAbsent(key, k -> newTaskGroup());
    }

    @Override
    public void run(ApplicationArguments args) {
        this.threadPoolExecutor = new ThreadPoolExecutor(localNodeConfig.getWorkPoolNum(),
                localNodeConfig.getWorkPoolNum() + 10, 10L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
