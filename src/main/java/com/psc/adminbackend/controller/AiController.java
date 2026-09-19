package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/summarize")
    public ResponseEntity<?> summarizeReviews(@RequestBody Map<String, List<String>> payload) {
        List<String> reviews = payload.getOrDefault("reviews", List.of());
        String summary = geminiService.summarizeReviews(reviews);
        return ResponseEntity.ok(Map.of("success", true, "summary", summary));
    }
}