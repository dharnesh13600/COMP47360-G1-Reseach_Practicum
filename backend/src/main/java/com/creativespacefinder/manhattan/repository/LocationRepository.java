package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// This interface will handle all the database stuff for Location entities
public interface LocationRepository extends JpaRepository<Location, UUID> {
}
