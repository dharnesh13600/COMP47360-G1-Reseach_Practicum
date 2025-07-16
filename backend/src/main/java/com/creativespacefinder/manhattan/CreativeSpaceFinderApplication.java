package com.creativespacefinder.manhattan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class CreativeSpaceFinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreativeSpaceFinderApplication.class, args);
    }
}

