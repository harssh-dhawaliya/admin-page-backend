package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.VenueSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/suggestions")
@CrossOrigin(origins = "http://localhost:5173")
public class VenueSuggestionController {

    @Autowired
    private VenueSuggestionService suggestionService;

    @PostMapping("/nearest-top-rated")
    public ResponseEntity<?> getNearestTopRatedVenues(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5.0") double radiusKm, // Added radius parameter with default
            @RequestBody List<VenueSuggestionService.VenueLocationDto> venues) {

        List<VenueSuggestionService.VenueSuggestionResult> suggestions =
                suggestionService.getNearestTopRatedVenues(lat, lon, radiusKm, venues);

        boolean isFallback = !suggestions.isEmpty() && suggestions.get(0).isFallback();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "fallbackUsed", isFallback,
                "message", isFallback ? "No 4.5+ venues found within radius. Showing nearest available venue." : "Qualifying venues found.",
                "suggestions", suggestions
        ));
    }
}