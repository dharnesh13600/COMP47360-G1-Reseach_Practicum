package com.creativespacefinder.manhattan.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;

@Service
public class ConnectionCleanupService {

    @Autowired
    private DataSource dataSource;

    /**
     * Monitor and clean up connections every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void monitorAndCleanConnections() {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();

                int active = poolBean.getActiveConnections();
                int idle = poolBean.getIdleConnections();
                int total = poolBean.getTotalConnections();

                // Log connection status
                System.out.println("Connection Monitor - Active: " + active + ", Idle: " + idle + ", Total: " + total);

                // Force cleanup if we have any idle connections
                if (idle > 0) {
                    System.out.println("Forcing cleanup of " + idle + " idle connections");
                    poolBean.softEvictConnections();
                }

                // Alert if we're using too many connections
                if (total > 1) {
                    System.out.println("WARNING: Using " + total + " connections (should be 1 max)");
                }
            }
        } catch (Exception e) {
            System.err.println("Error monitoring connections: " + e.getMessage());
        }
    }

    /**
     * Force cleanup on application shutdown
     */
    @PreDestroy
    public void forceCleanupOnShutdown() {
        System.out.println("Application shutting down - forcing connection cleanup");
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;

                // Force close all connections
                hikariDS.getHikariPoolMXBean().softEvictConnections();

                // Wait a moment for cleanup
                Thread.sleep(2000);

                // Close the pool completely
                hikariDS.close();

                System.out.println("Connection cleanup completed");
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown cleanup: " + e.getMessage());
        }
    }
}