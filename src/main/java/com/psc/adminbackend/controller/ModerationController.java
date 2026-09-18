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
@RequestMapping("/api/moderation")
@CrossOrigin(origins = "http://localhost:5173")
public class ModerationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 1. Fetch All Reports for the Moderation Console
    @GetMapping("/reports")
    public ResponseEntity<?> getReports(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String role = (String) claims.get("role");

            if (!"SUPER_ADMIN".equals(role) && !"CONTENT_MODERATOR".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
            }

            // Fetch reports ordered by newest first, applying a 48-hour rolling filter capability if needed
            String sql = "SELECT id, report_id, content_id, content_snippet, reported_user, reporter, severity, status, created_at FROM content_reports ORDER BY created_at DESC";
            List<Map<String, Object>> reports = jdbcTemplate.queryForList(sql);

            return ResponseEntity.ok(reports);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // 2. Update Report Status (e.g., Resolve, Review, or Dismiss)
    @PostMapping("/reports/{reportId}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable String reportId, @RequestBody Map<String, String> payload, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String newStatus = payload.get("status"); // Expected: PENDING, REVIEW, RESOLVED, DISMISSED

        try {
            String sql = "UPDATE content_reports SET status = ? WHERE report_id = ?";
            int rowsUpdated = jdbcTemplate.update(sql, newStatus, reportId);

            if (rowsUpdated > 0) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Report " + reportId + " updated to " + newStatus));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Report not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}