package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.entity.Location;
import com.creativespacefinder.manhattan.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// LocationController class is specified to control the application's map that will be displayed for locations
// Therefore, all locations are fetched in the database and are used to populate the map UI
@RestController // The annotation handles the HTTP request of the page and returns the JSON response of the map's markers/locations (only JSON and not HTML)
@RequestMapping("/api/locations")
public class LocationController {


    // This is dependency injection, where we access the locational data from the database
    private final LocationRepository locationRepository;

    // When this controller is actually made, it is given the LocationRepository so we can use it
    @Autowired // Spring automatically connects the repo!
    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    //This method runs when a GET request is sent to /api/locations (GetMapping)
    // It will return the list of all locations in the database
    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}
