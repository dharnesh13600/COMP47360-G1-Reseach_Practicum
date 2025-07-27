package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherForecastServiceErrorTests {

    private WeatherForecastService svc;
    private RestTemplate rest;

    @BeforeEach
    void setUp() {
        svc = new WeatherForecastService();
        rest = mock(RestTemplate.class);
        ReflectionTestUtils.setField(svc, "rest", rest);
        ReflectionTestUtils.setField(svc, "apiKey", "DUMMY");
    }

    @Test
    void getAvailableForecastDateTimes_malformedJson_throwsApiException() {
        when(rest.getForObject(anyString(), eq(String.class)))
                .thenReturn("{ not valid json ");

        assertThatThrownBy(svc::getAvailableForecastDateTimes)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Failed to extract forecast datetimes");
    }

    @Test
    void getWeatherForDateTime_onApiError_returnsDefault() {
        WeatherForecastService spy = spy(svc);
        doThrow(new RuntimeException("boom")).when(spy).get96HourForecast();

        LocalDateTime target = LocalDateTime.of(2025, 7, 20, 14, 0);
        var data = spy.getWeatherForDateTime(target);

        assertThat(data.getDateTime()).isEqualTo(target);
        // ← changed here:
        assertThat(data.getTemperature()).isEqualByComparingTo("70.0");
        assertThat(data.getCondition()).isEqualTo("Clear");
        assertThat(data.getDescription()).isEqualTo("clear sky");
    }
}
