package com.creativespacefinder.manhattan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "cacheWarmingExecutor")
    public Executor cacheWarmingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);           // Only 1 core thread
        executor.setMaxPoolSize(1);            // Only 1 max thread
        executor.setQueueCapacity(1);          // Only queue 1 additional task
        executor.setThreadNamePrefix("CacheWarming-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300); // Wait up to 5 minutes for cache warming to finish
        executor.initialize();

        System.out.println("CacheWarmingExecutor initialized - async cache warming enabled");
        return executor;
    }
}