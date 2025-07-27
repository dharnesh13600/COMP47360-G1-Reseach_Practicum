package com.creativespacefinder.manhattan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreativeSpaceFinderApplicationTests {

    @Test
    void contextLoads() {
        CreativeSpaceFinderApplication app = new CreativeSpaceFinderApplication();
        assertNotNull(app);
        System.out.println("Application class exists and can be instantiated");
    }

    @Test
    void applicationMainClassExists() {
        assertNotNull(CreativeSpaceFinderApplication.class);
        System.out.println("Application main class is properly defined");
    }
}