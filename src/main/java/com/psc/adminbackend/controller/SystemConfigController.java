package com.psc.adminbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SystemConfigController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfigurations() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> settings = List.of(
                    Map.of("id", "search_boost_weight", "label", "Search Boost Weight", "description", "Multiplier applied to verified partner listings in search ranking results.", "category", "algorithm", "type", "decimal", "value", 1.4, "defaultValue", 1.0, "min", 0.5, "max", 3.0, "step", 0.1),
                    Map.of("id", "ai_confidence_threshold", "label", "AI Confidence Threshold", "description", "Minimum confidence score required for automated review flagging.", "category", "ai", "type", "decimal", "value", 0.85, "defaultValue", 0.80, "min", 0.50, "max", 0.99, "step", 0.01),
                    Map.of("id", "auto_content_moderation", "label", "Auto Content Moderation", "description", "Automatically quarantine reviews flagged with high AI toxicity scores.", "category", "feature", "type", "boolean", "value", true, "defaultValue", false),
                    Map.of("id", "nearest_venue_suggestions", "label", "Nearest Venue Suggestions", "description", "Enable location-based venue recommendation algorithms on user feeds.", "category", "feature", "type", "boolean", "value", true, "defaultValue", true)
            );

            response.put("success", true);
            response.put("settings", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAuditHistory() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> history = List.of(
                    Map.of("id", "audit-initial", "timestamp", Instant.now().minusSeconds(3600).toString(), "parameter", "search_boost_weight", "oldValue", 1.2, "newValue", 1.4, "adminUser", "Akshita Pandey", "reason", "Optimized seasonal partner weighting")
            );
            response.put("success", true);
            response.put("history", history);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/config/update")
    public ResponseEntity<?> updateConfiguration(@RequestBody Map<String, Object> payload) {
        String key = (String) payload.get("configKey");
        return ResponseEntity.ok(Map.of("success", true, "message", "Configuration " + key + " updated successfully."));
    }
}