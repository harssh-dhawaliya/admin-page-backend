package com.psc.adminbackend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EventFeedService {

    public List<Map<String, Object>> getFilteredEvents(String location, String startDateStr, String endDateStr, List<Map<String, Object>> allEvents) {
        LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now();
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now().plusMonths(1);

        return allEvents.stream()
                // Filter by location (case-insensitive match)
                .filter(event -> {
                    if (location == null || location.isBlank()) return true;
                    String eventLocation = (String) event.getOrDefault("location", "");
                    return eventLocation.toLowerCase().contains(location.toLowerCase());
                })
                // Filter by date range (ensures past events or out-of-range events don't surface)
                .filter(event -> {
                    LocalDate eventDate = LocalDate.parse((String) event.get("date"));
                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                })
                .toList();
    }
}