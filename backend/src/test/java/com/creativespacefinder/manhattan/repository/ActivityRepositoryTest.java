package com.creativespacefinder.manhattan.repository;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.creativespacefinder.manhattan.entity.Activity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.yaml")
@DataJpaTest
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByNameIgnoreCase_Found() {
        Activity activity = new Activity("Swimming");
        entityManager.persistAndFlush(activity);

        Optional<Activity> found = activityRepository.findByNameIgnoreCase("swimming");
        assertTrue(found.isPresent());
        assertEquals("Swimming", found.get().getName());
    }

    @Test
    void findByNameIgnoreCase_NotFound() {
        Optional<Activity> found = activityRepository.findByNameIgnoreCase("NonExistent");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByNameIgnoreCase_True() {
        Activity activity = new Activity("Reading");
        entityManager.persistAndFlush(activity);

        boolean exists = activityRepository.existsByNameIgnoreCase("reading");
        assertTrue(exists);
    }

    @Test
    void existsByNameIgnoreCase_False() {
        boolean exists = activityRepository.existsByNameIgnoreCase("NonExistent");
        assertFalse(exists);
    }
}