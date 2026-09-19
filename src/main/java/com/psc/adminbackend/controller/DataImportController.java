package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/import")
@CrossOrigin(origins = "http://localhost:5173")
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    // Explicitly consume multipart form data to prevent 415 errors
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importFromFile(@RequestParam("file") MultipartFile file) {
        try {
            DataImportService.ImportedItineraryResult result = dataImportService.importFromFileUpload(file);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/email-confirmation")
    public ResponseEntity<?> importFromEmail(@RequestBody Map<String, String> emailPayload) {
        String emailBody = emailPayload.getOrDefault("emailBody", "");
        DataImportService.ImportedItineraryResult result = dataImportService.importFromEmailConfirmation(emailBody);
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    }
}