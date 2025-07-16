package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.WeatherForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherForecastController.class)
class WeatherForecastControllerTests {

    @Autowired MockMvc mvc;
    @MockBean WeatherForecastService wxSvc;

    @Test
    void availableDates_returnsList() throws Exception {
        when(wxSvc.getAvailableForecastDateTimes())
                .thenReturn(List.of(LocalDateTime.of(2025, 1, 1, 9, 0)));

        mvc.perform(get("/api/forecast/available-datetimes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }
}
