package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

// This interface handles all database access for Location entities
public interface LocationRepository extends JpaRepository<Location, UUID> {

    // This query is to fetch the top 5 locations
    @Query("SELECT l FROM Location l")
    List<Location> findTop5(Pageable pageable);
}
