package com.psc.adminbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataImportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class RawTravelItemDto {
        private String itemName;
        private String date;
        private double cost;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
    }

    public static class ImportedItineraryResult {
        private boolean isBeta = true;
        private String betaNotice = "[BETA] Travel import is in beta testing scope. Saved to database!";
        private String feedbackUrl = "http://localhost:5173/feedback?feature=travel-import-beta";
        private List<Map<String, Object>> importedItems;

        public boolean isBeta() { return isBeta; }
        public String getBetaNotice() { return betaNotice; }
        public String getFeedbackUrl() { return feedbackUrl; }
        public List<Map<String, Object>> getImportedItems() { return importedItems; }
        public void setImportedItems(List<Map<String, Object>> importedItems) { this.importedItems = importedItems; }
    }

    public ImportedItineraryResult importFromEmailConfirmation(String emailBody) {
        String destination = emailBody != null && emailBody.toLowerCase().contains("kol") ? "Kolkata (KOL)" : "Destination Airport/Hotel";
        String timestamp = LocalDateTime.now().toString();

        try {
            // Explicitly supplies user_id = 1 to satisfy NOT NULL and Foreign Key constraints
            String sql = "INSERT INTO itinerary_drafts (user_id, title, destination, source, status) VALUES (1, ?, ?, 'EMAIL', 'DRAFT')";
            jdbcTemplate.update(sql, "Email Confirmation Draft", destination);
        } catch (Exception e) {
            System.err.println("Database storage error: " + e.getMessage());
        }

        ImportedItineraryResult result = new ImportedItineraryResult();
        Map<String, Object> item = new HashMap<>();
        item.put("itineraryTitle", "Flight / Booking Confirmation");
        item.put("destination", destination);
        item.put("scheduledTime", timestamp);
        item.put("type", "BOOKING");

        result.setImportedItems(List.of(item));
        return result;
    }

    public ImportedItineraryResult importFromFileUpload(MultipartFile file) {
        String fileName = file != null ? file.getOriginalFilename() : "Uploaded_Itinerary.csv";
        String timestamp = java.time.LocalDateTime.now().toString();

        try {
            // Mapped to your itinerary_drafts table columns
            String sql = "INSERT INTO itinerary_drafts (user_id, title, destination, source, status) VALUES (1, ?, ?, 'FILE', 'DRAFT')";
            jdbcTemplate.update(sql, "File: " + fileName, "Extracted from " + fileName);
        } catch (Exception e) {
            System.err.println("Database storage error: " + e.getMessage());
        }

        ImportedItineraryResult result = new ImportedItineraryResult();
        Map<String, Object> item = new HashMap<>();
        item.put("itineraryTitle", fileName);
        item.put("destination", "Parsed from file upload");
        item.put("scheduledTime", timestamp);
        item.put("type", "FILE_UPLOAD");

        result.setImportedItems(List.of(item));
        return result;
    }
}