package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.*;
import com.creativespacefinder.manhattan.exception.ApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherForecastServiceTests {

    WeatherForecastService service;
    RestTemplate restMock;

    @BeforeEach
    void init() {
        service   = new WeatherForecastService();
        restMock  = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "rest", restMock);
        ReflectionTestUtils.setField(service, "apiKey", "dummy");
    }

    @Test
    void ok96hForecast_returnsObject() {
        ForecastResponse fake = new ForecastResponse();
        when(restMock.getForObject(anyString(), eq(ForecastResponse.class))).thenReturn(fake);

        assertThat(service.get96HourForecast()).isSameAs(fake);
    }

    @Test
    void openWeather404_wrapsApiException() {
        when(restMock.getForObject(anyString(), eq(ForecastResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND,"NF", HttpHeaders.EMPTY,null,null));

        assertThatThrownBy(service::get96HourForecast)
                .isInstanceOf(ApiException.class);
    }

    @Test
    void malformedJson_onAvailableTimes_throws() {
        when(restMock.getForObject(anyString(), eq(String.class))).thenReturn("{\"foo\":\"bar\"}");

        assertThatThrownBy(service::getAvailableForecastDateTimes)
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getWeatherForDateTime_fallback70F() {
        ForecastResponse empty = new ForecastResponse();
        empty.setHourly(List.of());
        when(restMock.getForObject(anyString(), eq(ForecastResponse.class))).thenReturn(empty);

        WeatherData data = service.getWeatherForDateTime(LocalDateTime.now());

        assertThat(data.getTemperature()).isEqualByComparingTo(BigDecimal.valueOf(70));
    }

    @Test
    void getAvailableForecastDateTimes_success_returnsCorrectDateTimes() throws Exception {
        long epoch1 = 1625140800L;
        long epoch2 = 1625144400L;
        String json = String.format(
                "{\"list\":[{\"dt\":%d},{\"dt\":%d}]}", epoch1, epoch2
        );
        when(restMock.getForObject(anyString(), eq(String.class))).thenReturn(json);

        List<LocalDateTime> result = service.getAvailableForecastDateTimes();

        LocalDateTime expected1 = Instant
                .ofEpochSecond(epoch1)
                .atZone(ZoneId.of("America/New_York"))
                .toLocalDateTime();
        LocalDateTime expected2 = Instant
                .ofEpochSecond(epoch2)
                .atZone(ZoneId.of("America/New_York"))
                .toLocalDateTime();

        assertThat(result)
                .hasSize(2)
                .containsExactly(expected1, expected2);
    }
}
