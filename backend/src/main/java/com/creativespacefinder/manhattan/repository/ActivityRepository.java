package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    /**
     * Find activity by name (case-insensitive)
     */
    Optional<Activity> findByNameIgnoreCase(String name);

    /** Look-up by activity name (e.g. “Street photography”, “Filmmaking”, …). */
    Optional<Activity> findByName(String name);

    /**
     * Check if activity exists by name
     */
    boolean existsByNameIgnoreCase(String name);
}

