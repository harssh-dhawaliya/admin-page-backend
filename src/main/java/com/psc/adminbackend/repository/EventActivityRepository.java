package com.psc.adminbackend.repository;

import com.psc.adminbackend.entity.EventActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity, Long> {

    // Criterion 1: Date range boundary so past events don't surface (eventDate >= currentDateTime)
    // Criterion 2: Optional filter by location / itinerary destination (venueId)
    @Query("SELECT e FROM EventActivity e WHERE e.eventDate >= :currentDateTime AND e.eventDate BETWEEN :startDate AND :endDate AND (:venueId IS NULL OR e.venueId = :venueId)")
    List<EventActivity> findRelevantEvents(
            @Param("currentDateTime") LocalDateTime currentDateTime,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("venueId") Long venueId
    );
}