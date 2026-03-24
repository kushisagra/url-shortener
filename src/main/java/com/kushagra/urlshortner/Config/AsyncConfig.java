package com.kushagra.urlshortner.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core threads always alive — ready to process analytics instantly
        executor.setCorePoolSize(2);

        // Max threads under heavy load
        executor.setMaxPoolSize(5);

        // Queue size — if all 5 threads busy, queue up to 100 tasks
        executor.setQueueCapacity(100);

        // Thread name prefix — visible in logs, makes debugging easier
        executor.setThreadNamePrefix("analytics-");

        // On shutdown, wait for queued analytics to finish (don't lose data)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}