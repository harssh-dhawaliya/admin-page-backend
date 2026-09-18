package com.psc.adminbackend.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "http://localhost:5173")
public class KycController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 1. Partner Submits KYC Documents
    @PostMapping("/submit")
    public ResponseEntity<?> submitKyc(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("user_id").toString());
            String businessName = (String) payload.get("business_name");
            String docType = (String) payload.get("document_type");
            String docRef = (String) payload.get("document_reference");

            String sql = "INSERT INTO kyc_submissions (user_id, business_name, document_type, document_reference, status) VALUES (?, ?, ?, ?, 'PENDING')";
            jdbcTemplate.update(sql, userId, businessName, docType, docRef);

            // Also update the user's general kyc_status to PENDING
            jdbcTemplate.update("UPDATE users SET kyc_status = 'PENDING' WHERE id = ?", userId);

            return ResponseEntity.ok(Map.of("success", true, "message", "KYC submitted successfully and pending review."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // 2. Admin Views the Pending KYC Queue (Restricted to Admins/Super Admins)
    @GetMapping("/queue")
    public ResponseEntity<?> getKycQueue(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        try {
            String token = authHeader.replace("Bearer ", "");

            // Fixed JWT parsing using the modern verifyWith pipeline
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = (String) claims.get("role");

            if (!"SUPER_ADMIN".equals(role) && !"CONTENT_MODERATOR".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
            }

            String sql = "SELECT k.id, k.user_id, u.email, k.business_name, k.document_type, k.document_reference, k.status, k.submitted_at " +
                    "FROM kyc_submissions k JOIN users u ON k.user_id = u.id WHERE k.status = 'PENDING'";

            List<Map<String, Object>> queue = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(queue);

        } catch (Exception e) {
            System.err.println("JWT Parsing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // 3. Admin Approves or Rejects a KYC Submission
    @PostMapping("/review/{id}")
    public ResponseEntity<?> reviewKyc(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status"); // Expected: 'VERIFIED' or 'REJECTED'

        try {
            // Get user_id associated with this KYC submission
            Map<String, Object> kycRow = jdbcTemplate.queryForMap("SELECT user_id FROM kyc_submissions WHERE id = ?", id);
            Long userId = ((Number) kycRow.get("user_id")).longValue();

            // Update KYC submission status
            jdbcTemplate.update("UPDATE kyc_submissions SET status = ? WHERE id = ?", status, id);

            // Update user table kyc_status
            jdbcTemplate.update("UPDATE users SET kyc_status = ? WHERE id = ?", status, userId);

            return ResponseEntity.ok(Map.of("success", true, "message", "KYC status updated to " + status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}