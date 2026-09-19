package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.EventSurfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventSurfaceController {

    @Autowired
    private EventSurfaceService eventSurfaceService;

    @GetMapping("/surface")
    public ResponseEntity<?> getSurfaceEventsForItinerary(
            @RequestParam String destination,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Mock database repository call for active events
        List<Map<String, Object>> masterEventsList = List.of(
                Map.of("title", "Paris Night Market", "location", "Paris", "date", "2026-10-02"),
                Map.of("title", "Historical Walking Tour", "location", "Paris", "date", "2026-10-04"),
                Map.of("title", "Old Festival (Past)", "location", "Paris", "date", "2024-05-10")
        );

        List<Map<String, Object>> surfacedEvents = eventSurfaceService.getEventsForItinerary(
                destination, startDate, endDate, masterEventsList
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "destination", destination,
                "matchedEventsCount", surfacedEvents.size(),
                "data", surfacedEvents
        ));
    }
}