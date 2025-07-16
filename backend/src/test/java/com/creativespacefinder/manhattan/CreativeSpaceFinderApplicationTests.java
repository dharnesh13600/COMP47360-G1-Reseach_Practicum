package com.creativespacefinder.manhattan;

import com.creativespacefinder.manhattan.service.WeatherForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // ✅ This is what tells Spring to load application-test.yaml
@TestPropertySource("classpath:application-test.yaml")
class CreativeSpaceFinderApplicationTests {

    @MockBean
    private WeatherForecastService weatherForecastService;

    // Since WeatherCacheRepository doesn't exist in main and you don't want to create it,
    // this @MockBean and its corresponding import must be removed or commented out.
    // @MockBean
    // private WeatherCacheRepository weatherCacheRepository;

    @Autowired
    Environment env;

    @Test
    void contextLoads() {
        System.out.println("✅ Active Profiles: " + String.join(", ", env.getActiveProfiles()));
        System.out.println("🔑 openweather.api-key: " + env.getProperty("openweather.api-key"));
        System.out.println("🔗 spring.datasource.url: " + env.getProperty("spring.datasource.url"));
    }
}