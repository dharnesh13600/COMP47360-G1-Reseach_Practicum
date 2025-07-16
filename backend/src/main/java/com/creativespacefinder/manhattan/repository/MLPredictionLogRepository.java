package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.MLPredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MLPredictionLogRepository extends JpaRepository<MLPredictionLog, UUID> {
}