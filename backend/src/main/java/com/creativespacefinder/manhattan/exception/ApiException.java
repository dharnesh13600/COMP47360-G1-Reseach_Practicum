package com.creativespacefinder.manhattan.exception;

// This returns a well written and clean JSON, if I try to connect to OpenWeather API and it fails
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}