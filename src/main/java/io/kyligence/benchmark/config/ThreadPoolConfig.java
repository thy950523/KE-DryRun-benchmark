package io.kyligence.benchmark.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kyligence.benchmark.BenchmarkConfig;
import lombok.val;

@Configuration
public class ThreadPoolConfig {

    @Autowired
    BenchmarkConfig config;

    @Bean("executor")
    public ThreadPoolExecutor threadPoolExecutor() {
        val threadPoolExecutor = new ThreadPoolExecutor(config.CONCURRENCY, config.CONCURRENCY, 1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(config.QUEUE_SIZE));
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }
}
