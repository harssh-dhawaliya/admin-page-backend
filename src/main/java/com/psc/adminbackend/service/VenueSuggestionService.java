package com.psc.adminbackend.service;

import com.psc.adminbackend.entity.VenueReview;
import com.psc.adminbackend.repository.VenueReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VenueSuggestionService {

    @Autowired
    private VenueReviewRepository reviewRepository;

    public record VenueLocationDto(Long venueId, double latitude, double longitude) {}
    public record VenueSuggestionResult(Long venueId, double averageRating, double distanceKm, boolean isFallback) {}

    public List<VenueSuggestionResult> getNearestTopRatedVenues(
            double userLat, double userLon, double maxRadiusKm, List<VenueLocationDto> venuesWithLocations) {

        List<VenueReview> allReviews = reviewRepository.findAll();

        // Calculate average rating per venue from reviews
        Map<Long, Double> venueAverages = allReviews.stream()
                .collect(Collectors.groupingBy(
                        VenueReview::getVenueId,
                        Collectors.averagingInt(VenueReview::getRating)
                ));

        // 1. Try to find 4.5+ venues within the specified radius
        List<VenueSuggestionResult> qualifyingVenues = venuesWithLocations.stream()
                .map(v -> {
                    double distance = calculateHaversineDistance(userLat, userLon, v.latitude(), v.longitude());
                    double avgRating = venueAverages.getOrDefault(v.venueId(), 0.0);
                    return new VenueSuggestionResult(v.venueId(), avgRating, distance, false);
                })
                .filter(v -> v.distanceKm() <= maxRadiusKm && v.averageRating() >= 4.5)
                .sorted(Comparator.comparingDouble(VenueSuggestionResult::distanceKm))
                .toList();

        // 2. Graceful Fallback: If no 4.5+ venue exists nearby, fall back to the absolute nearest venue regardless of rating
        if (qualifyingVenues.isEmpty() && !venuesWithLocations.isEmpty()) {
            VenueLocationDto nearestOverall = venuesWithLocations.stream()
                    .min(Comparator.comparingDouble(v -> calculateHaversineDistance(userLat, userLon, v.latitude(), v.longitude())))
                    .get();

            double distance = calculateHaversineDistance(userLat, userLon, nearestOverall.latitude(), nearestOverall.longitude());
            double avgRating = venueAverages.getOrDefault(nearestOverall.venueId(), 0.0);

            // Return as a fallback result
            return List.of(new VenueSuggestionResult(nearestOverall.venueId(), avgRating, distance, true));
        }

        return qualifyingVenues;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}