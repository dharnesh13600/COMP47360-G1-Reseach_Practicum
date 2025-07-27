package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ForecastResponseUnknownPropsTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialization_withExtraField_doesNotFail() throws Exception {
        String json = """
        {
          "list": [
            {
              "dt": 1620000000,
              "main": { "temp": 55.5 },
              "weather": [ { "id":800, "main":"Clear", "description":"clear sky", "icon":"01d" } ],
              "extra_field": "surprise!"
            }
          ],
          "extra_top": 123
        }
        """;

        ForecastResponse resp = mapper.readValue(json, ForecastResponse.class);

        assertThat(resp.getHourly())
                .hasSize(1)
                .first()
                .satisfies(h -> {
                    assertThat(h.getTemp()).isEqualTo(55.5);
                    assertThat(h.getCondition()).isEqualTo("Clear");
                });
    }
}
