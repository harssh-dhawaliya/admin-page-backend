package com.psc.adminbackend.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class BreakVenueService {

    public Map<String, Object> findNearestBreakVenue(double userLat, double userLon, double radiusKm, List<Map<String, Object>> allVenues) {
        // Step 1: Filter venues within radius and matching category (restaurant/cafe)
        List<Map<String, Object>> nearbyQualifyingVenues = allVenues.stream()
                .filter(v -> isRestaurantOrCafe(v))
                .filter(v -> calculateDistance(userLat, userLon, getLat(v), getLon(v)) <= radiusKm)
                .filter(v -> getRating(v) >= 4.5)
                .sorted((v1, v2) -> {
                    // Sort by nearest distance first
                    double dist1 = calculateDistance(userLat, userLon, getLat(v1), getLon(v1));
                    double dist2 = calculateDistance(userLat, userLon, getLat(v2), getLon(v2));
                    return Double.compare(dist1, dist2);
                })
                .toList();

        // Step 2: Graceful Fallback if no 4.5+ venue exists nearby
        if (nearbyQualifyingVenues.isEmpty()) {
            // Fallback: relax rating constraint to find the absolute nearest venue regardless of rating
            Map<String, Object> absoluteNearest = allVenues.stream()
                    .filter(v -> isRestaurantOrCafe(v))
                    .min((v1, v2) -> {
                        double d1 = calculateDistance(userLat, userLon, getLat(v1), getLon(v1));
                        double d2 = calculateDistance(userLat, userLon, getLat(v2), getLon(v2));
                        return Double.compare(d1, d2);
                    })
                    .orElse(null);

            if (absoluteNearest != null) {
                return Map.of(
                        "success", true,
                        "fallbackUsed", true,
                        "message", "No 4.5+ star venues found within " + radiusKm + "km. Showing the nearest available alternative.",
                        "venue", absoluteNearest
                );
            }

            return Map.of("success", false, "message", "No restaurants or cafes found nearby.");
        }

        // Return top nearest 4.5+ venue
        return Map.of(
                "success", true,
                "fallbackUsed", false,
                "venue", nearbyQualifyingVenues.get(0)
        );
    }

    // Helper math / mapping functions (Haversine distance formula can be used here)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Simplified placeholder for Haversine formula calculation in kilometers
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 111;
    }

    private boolean isRestaurantOrCafe(Map<String, Object> v) {
        String type = (String) v.getOrDefault("type", "");
        return type.equalsIgnoreCase("restaurant") || type.equalsIgnoreCase("cafe");
    }

    private double getLat(Map<String, Object> v) { return Double.parseDouble(v.getOrDefault("lat", 0.0).toString()); }
    private double getLon(Map<String, Object> v) { return Double.parseDouble(v.getOrDefault("lon", 0.0).toString()); }
    private double getRating(Map<String, Object> v) { return Double.parseDouble(v.getOrDefault("rating", 0.0).toString()); }
}