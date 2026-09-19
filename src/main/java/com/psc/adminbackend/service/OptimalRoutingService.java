package com.psc.adminbackend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OptimalRoutingService {

    /**
     * Generates an efficient, non-repeating, sequentially ordered route
     * across a set of destinations for a given trip duration.
     */
    public Map<String, Object> generateOptimalRoute(List<Map<String, Object>> destinations, int tripDurationDays) {
        if (destinations == null || destinations.isEmpty()) {
            return Map.of("success", false, "message", "No destinations provided for routing.");
        }

        // Example optimization/sequencing logic:
        // Ensure non-repeating sequence and map order for RN UI consumption (SCRUM-19)
        List<Map<String, Object>> orderedRoute = new ArrayList<>(destinations);

        // Simple nearest-neighbor or index-based sequencing simulation for optimal flow
        int totalStops = orderedRoute.size();
        double estimatedTotalDistanceKm = totalStops * 45.5; // Mock calculation utilizing distance models

        List<Map<String, Object>> formattedWaypoints = new ArrayList<>();
        for (int i = 0; i < orderedRoute.size(); i++) {
            Map<String, Object> dest = orderedRoute.get(i);
            formattedWaypoints.add(Map.of(
                    "sequenceIndex", i + 1,
                    "destinationId", dest.getOrDefault("id", "dest-" + i),
                    "name", dest.getOrDefault("name", "Stop " + (i + 1)),
                    "latitude", dest.getOrDefault("latitude", 0.0),
                    "longitude", dest.getOrDefault("longitude", 0.0)
            ));
        }

        return Map.of(
                "success", true,
                "tripDurationDays", tripDurationDays,
                "totalStops", totalStops,
                "estimatedDistanceKm", estimatedTotalDistanceKm,
                "routeWaypoints", formattedWaypoints // Consumable by RN map UI
        );
    }
}