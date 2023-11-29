package io.kyligence.benchmark.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.val;

@Configuration
public class ThreadPoolConfig {

    @Value("${concurrency:4}")
    private Integer CORE_POOL_SIZE;
    @Value("${concurrency:4}")
    private Integer MAX_POOL_SIZE;
    @Value("${queue-size:2048}")
    private Integer QUEUE_SIZE;

    @Bean("executor")
    public ThreadPoolExecutor threadPoolExecutor() {
        val threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 15, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_SIZE));
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }
}
