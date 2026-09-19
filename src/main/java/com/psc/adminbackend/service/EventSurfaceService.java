package com.psc.adminbackend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EventSurfaceService {

    public List<Map<String, Object>> getEventsForItinerary(String destination, String tripStartDateStr, String tripEndDateStr, List<Map<String, Object>> allEvents) {
        LocalDate today = LocalDate.now();
        LocalDate tripStartDate = tripStartDateStr != null ? LocalDate.parse(tripStartDateStr) : today;
        LocalDate tripEndDate = tripEndDateStr != null ? LocalDate.parse(tripEndDateStr) : today.plusDays(7);

        return allEvents.stream()
                // Match the itinerary destination
                .filter(event -> {
                    if (destination == null || destination.isBlank()) return true;
                    String eventLocation = (String) event.getOrDefault("location", "");
                    return eventLocation.equalsIgnoreCase(destination);
                })
                // Ensure event falls within the active itinerary date range and is not in the past
                .filter(event -> {
                    LocalDate eventDate = LocalDate.parse((String) event.get("date"));
                    // Exclude past events relative to today AND ensure it fits within the trip window
                    return !eventDate.isBefore(today) && !eventDate.isBefore(tripStartDate) && !eventDate.isAfter(tripEndDate);
                })
                .toList();
    }
}