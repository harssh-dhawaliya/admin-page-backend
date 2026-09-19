package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.OptimalRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/routing")
@CrossOrigin(origins = "http://localhost:5173")
public class AiRoutingController {

    @Autowired
    private OptimalRoutingService optimalRoutingService;

    @PostMapping("/optimize")
    public ResponseEntity<?> getOptimalRoute(@RequestBody Map<String, Object> payload) {
        try {
            int tripDurationDays = Integer.parseInt(payload.getOrDefault("tripDurationDays", 3).toString());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> destinations = (List<Map<String, Object>>) payload.getOrDefault("destinations", List.of());

            Map<String, Object> routeResult = optimalRoutingService.generateOptimalRoute(destinations, tripDurationDays);

            return ResponseEntity.ok(routeResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}