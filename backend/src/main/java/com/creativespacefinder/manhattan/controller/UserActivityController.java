package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.UserActivityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class UserActivityController {

    @PostMapping
    public ResponseEntity<String> handleActivity(@Valid @RequestBody UserActivityRequest request) {
        return ResponseEntity.ok("Activity received successfully");
    }
}
