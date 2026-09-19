package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.DestinationWeightingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prioritization")
@CrossOrigin(origins = "http://localhost:5173")
public class PrioritizationController {

    @Autowired
    private DestinationWeightingService destinationWeightingService;

    @PostMapping("/destinations")
    public ResponseEntity<?> getPrioritizedDestinations(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> destinations = (List<Map<String, Object>>) payload.getOrDefault("destinations", List.of());

        List<Map<String, Object>> prioritized = destinationWeightingService.prioritizeDestinations(destinations);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", prioritized.size(),
                "data", prioritized
        ));
    }
}