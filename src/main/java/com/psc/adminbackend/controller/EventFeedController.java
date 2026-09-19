package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.EventFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventFeedController {

    @Autowired
    private EventFeedService eventFeedService;

    @GetMapping("/feed")
    public ResponseEntity<?> getEventFeed(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // Mock database or service call for all active events
        List<Map<String, Object>> mockEvents = List.of(
                Map.of("title", "Spring Music Festival", "location", "Paris", "date", "2026-10-05"),
                Map.of("title", "Food & Wine Expo", "location", "London", "date", "2026-10-12")
        );

        List<Map<String, Object>> filteredEvents = eventFeedService.getFilteredEvents(location, startDate, endDate, mockEvents);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", filteredEvents.size(),
                "data", filteredEvents
        ));
    }
}