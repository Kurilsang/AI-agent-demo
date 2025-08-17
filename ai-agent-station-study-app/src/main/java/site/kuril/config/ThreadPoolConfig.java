package site.kuril.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置
 * 为数据加载策略提供异步处理能力
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("threadPoolExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                // 核心线程数
                10,
                // 最大线程数
                20,
                // 空闲线程存活时间
                60L,
                TimeUnit.SECONDS,
                // 工作队列
                new LinkedBlockingQueue<>(1000),
                // 线程工厂
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "armory-pool-" + threadNumber.getAndIncrement());
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                // 拒绝策略
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

}
