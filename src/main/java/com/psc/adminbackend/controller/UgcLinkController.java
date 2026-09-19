package com.psc.adminbackend.controller;

import com.psc.adminbackend.entity.UgcLink;
import com.psc.adminbackend.service.UgcLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ugc")
@CrossOrigin(origins = "http://localhost:5173")
public class UgcLinkController {

    @Autowired
    private UgcLinkService ugcLinkService;

    @PostMapping("/links")
    public ResponseEntity<?> submitUgcLink(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.getOrDefault("userId", 1).toString());
            String destinationId = (String) payload.getOrDefault("destinationId", "paris-france");
            String url = (String) payload.get("url");
            String platform = (String) payload.getOrDefault("platform", "Instagram");

            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL is required."));
            }

            UgcLink savedLink = ugcLinkService.submitUgcLink(userId, destinationId, url, platform);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "UGC link submitted and evaluated through zero-tolerance moderation.",
                    "data", savedLink
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}