package com.creativespacefinder.manhattan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * This is the main application class for the Manhattan Muse
 * We set up auto config, caching and scheduling here
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CreativeSpaceFinderApplication {
    /**
     * The application is Bootstrapped with the embedded Tomcat server
     * Everything is run from here
     */
    public static void main(String[] args) {
        SpringApplication.run(CreativeSpaceFinderApplication.class, args);
    }
}