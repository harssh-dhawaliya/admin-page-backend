package com.psc.adminbackend.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DestinationWeightingService {

    /**
     * Discrete, testable weighting function for SCRUM-30.
     * Prioritizes destinations rated 4.5+ stars higher in output, all else equal.
     */
    public List<Map<String, Object>> prioritizeDestinations(List<Map<String, Object>> destinations) {
        if (destinations == null || destinations.isEmpty()) {
            return List.of();
        }

        return destinations.stream()
                .sorted((d1, d2) -> {
                    double rating1 = getRating(d1);
                    double rating2 = getRating(d2);

                    boolean isHighRated1 = rating1 >= 4.5;
                    boolean isHighRated2 = rating2 >= 4.5;

                    // Priority rule: 4.5+ rated destinations rank higher
                    if (isHighRated1 && !isHighRated2) {
                        return -1; // d1 comes first
                    }
                    if (!isHighRated1 && isHighRated2) {
                        return 1;  // d2 comes first
                    }

                    // Secondary sort: highest exact rating descending, all else equal
                    return Double.compare(rating2, rating1);
                })
                .collect(Collectors.toList());
    }

    private double getRating(Map<String, Object> destination) {
        Object ratingObj = destination.get("rating");
        if (ratingObj == null) return 0.0;
        try {
            return Double.parseDouble(ratingObj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}