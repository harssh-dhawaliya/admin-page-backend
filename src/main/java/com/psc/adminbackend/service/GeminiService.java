package com.psc.adminbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String geminiUrl;

    public String summarizeReviews(List<String> reviews) {
        // Gracefully handle zero or sparse reviews
        if (reviews == null || reviews.isEmpty()) {
            return "No reviews available yet for this destination to summarize.";
        }
        if (reviews.size() < 2) {
            return "Only one review is available: \"" + reviews.get(0) + "\". More reviews are needed for a comprehensive summary.";
        }

        String combinedReviews = String.join("\n- ", reviews);
        String prompt = "Please summarize the following venue/travel reviews into a concise paragraph highlighting key praise and common complaints:\n- " + combinedReviews;

        RestTemplate restTemplate = new RestTemplate();
        String url = geminiUrl + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct Gemini request payload structure
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse response body safely
                Map responseBody = response.getBody();
                List<Map> candidates = (List<Map>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map content = (Map) candidates.get(0).get("content");
                    List<Map> parts = (List<Map>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }

        return "Could not generate summary.";
    }
}