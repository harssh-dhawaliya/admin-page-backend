package com.psc.adminbackend.controller;

import com.psc.adminbackend.entity.VenueReview;
import com.psc.adminbackend.repository.VenueReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    @Autowired
    private VenueReviewRepository reviewRepository;

    @PostMapping
    public ResponseEntity<?> submitOrUpdateReview(@RequestBody VenueReview incomingReview) {
        // Validate rating range (1 to 5)
        if (incomingReview.getRating() < 1 || incomingReview.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Rating must be between 1 and 5."));
        }

        // Enforce one active review per user per destination via edit/upsert behavior
        Optional<VenueReview> existingReview = reviewRepository.findByUserIdAndVenueId(
                incomingReview.getUserId(), incomingReview.getVenueId()
        );

        VenueReview savedReview;
        if (existingReview.isPresent()) {
            // Edit existing review
            VenueReview reviewToUpdate = existingReview.get();
            reviewToUpdate.setRating(incomingReview.getRating());
            reviewToUpdate.setReviewText(incomingReview.getReviewText());
            reviewToUpdate.setCreatedAt(LocalDateTime.now());
            savedReview = reviewRepository.save(reviewToUpdate);
        } else {
            // Create new review
            savedReview = reviewRepository.save(incomingReview);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Review saved successfully", "review", savedReview));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<?> getReviewsForVenue(@PathVariable Long venueId) {
        List<VenueReview> reviews = reviewRepository.findByVenueId(venueId);
        return ResponseEntity.ok(Map.of("success", true, "reviews", reviews));
    }
}