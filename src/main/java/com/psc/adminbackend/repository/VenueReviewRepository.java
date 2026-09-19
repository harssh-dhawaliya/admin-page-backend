package com.psc.adminbackend.repository;

import com.psc.adminbackend.entity.VenueReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueReviewRepository extends JpaRepository<VenueReview, Long> {
    List<VenueReview> findByVenueId(Long venueId);

    // Check for an existing review by this user for this venue (enforces one review per user/destination)
    Optional<VenueReview> findByUserIdAndVenueId(Long userId, Long venueId);
}