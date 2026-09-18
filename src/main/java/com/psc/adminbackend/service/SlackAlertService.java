package com.psc.adminbackend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SlackAlertService {

    // Replace this with your actual Slack Incoming Webhook URL when ready
    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/T00/B00/XXXXX";

    public void sendAlert(String title, String message, String alertType) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Choose emoji based on alert type
            String emoji = "ℹ️";
            if ("CRITICAL".equalsIgnoreCase(alertType)) emoji = "🚨";
            if ("FINANCIAL".equalsIgnoreCase(alertType)) emoji = "💰";
            if ("SECURITY".equalsIgnoreCase(alertType)) emoji = "🔒";

            String markdownText = String.format("*%s [TourBhook Admin Alert]*\n*Event:* %s\n> %s", emoji, title, message);
            Map<String, String> body = Map.of("text", markdownText);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            // Send webhook request
            // restTemplate.postForEntity(SLACK_WEBHOOK_URL, request, String.class);

            System.out.println("[Slack Alert Simulated]: " + markdownText);
        } catch (Exception e) {
            System.err.println("Failed to send Slack alert: " + e.getMessage());
        }
    }
}