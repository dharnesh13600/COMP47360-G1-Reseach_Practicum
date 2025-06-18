package com.creativespacefinder.manhattan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UserActivityRequest {

    @NotBlank(message = "Activity type is required")
    private String activity;

    @NotNull(message = "Date and time must be provided")
    private LocalDateTime dateTime;

    // Setters and getters

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
