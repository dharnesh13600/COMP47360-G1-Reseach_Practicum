package com.creativespacefinder.manhattan.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.format.DateTimeParseException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/throw/api")
        void throwApi() {
            throw new ApiException("api down");
        }

        @PostMapping("/throw/json")
        void throwJson() {
            throw new HttpMessageNotReadableException("Invalid request format: malformed");
        }

        @GetMapping("/throw/method")
        void throwMethod() throws HttpRequestMethodNotSupportedException {
            throw new HttpRequestMethodNotSupportedException("PATCH");
        }

        @GetMapping("/throw/media")
        void throwMedia() throws HttpMediaTypeNotSupportedException {
            throw new HttpMediaTypeNotSupportedException("text/plain");
        }

        @GetMapping("/throw/notfound")
        void throwNotFound() throws NoHandlerFoundException {
            throw new NoHandlerFoundException("GET", "/foo", null);
        }

        @GetMapping("/throw/datetime")
        void throwDate() {
            throw new DateTimeParseException("bad date", "xyz", 0);
        }

        @GetMapping("/throw/other")
        void throwOther() {
            throw new RuntimeException("oh no");
        }
    }

    @Test @DisplayName("API_EXCEPTION → 502 BAD_GATEWAY")
    void whenApiException_thenBadGateway() throws Exception {
        mvc.perform(get("/throw/api"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("API_ERROR"))
                .andExpect(jsonPath("$.message").value("api down"));
    }

    @Test @DisplayName("Malformed JSON → 400 BAD_REQUEST")
    void whenHttpMessageNotReadable_thenBadRequest() throws Exception {
        mvc.perform(post("/throw/json")
                        .content("not-json")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("Invalid request format")));
    }

    @Test @DisplayName("Unsupported Method → 405 METHOD_NOT_ALLOWED")
    void whenMethodNotSupported_thenMethodNotAllowed() throws Exception {
        mvc.perform(get("/throw/method"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message", containsString("PATCH")));
    }

    @Test @DisplayName("Unsupported Media → 415 UNSUPPORTED_MEDIA_TYPE")
    void whenMediaTypeNotSupported_thenUnsupportedMediaType() throws Exception {
        mvc.perform(get("/throw/media"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message", containsString("text/plain")));
    }

    @Test @DisplayName("No Handler → 404 NOT_FOUND")
    void whenNoHandlerFound_thenNotFound() throws Exception {
        mvc.perform(get("/throw/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Endpoint not found")));
    }

    @Test @DisplayName("DateTimeParse → 400 BAD_REQUEST")
    void whenDateTimeParse_thenBadRequest() throws Exception {
        mvc.perform(get("/throw/datetime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_DATETIME"))
                .andExpect(jsonPath("$.message", containsString("Invalid date format")));
    }

    @Test @DisplayName("Generic Exception → 500 INTERNAL_SERVER_ERROR")
    void whenGenericException_thenInternalError() throws Exception {
        mvc.perform(get("/throw/other"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("oh no"));
    }
}
