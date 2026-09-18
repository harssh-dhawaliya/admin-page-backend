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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ingestion")
@CrossOrigin(origins = "http://localhost:5173")
public class IngestionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Quick Browser Test Route (GET)
    @GetMapping("/test-insert")
    public ResponseEntity<?> testInsert() {
        String sql = "INSERT INTO ingested_media_links (partner_id, platform, original_url, media_id, status) VALUES (?, ?, ?, ?, 'PENDING_REVIEW')";
        jdbcTemplate.update(sql, 1L, "INSTAGRAM", "https://www.instagram.com/reel/TEST123XYZ/", "TEST123XYZ");
        return ResponseEntity.ok(Map.of("success", true, "message", "Test row inserted successfully via browser!"));
    }

    // 1. Submit and Parse Social Media Link
    @PostMapping("/submit-link")
    public ResponseEntity<?> submitLink(@RequestBody Map<String, Object> payload, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Long userId = Long.valueOf(claims.get("id").toString());

            String url = (String) payload.get("url");
            if (url == null || url.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "URL cannot be empty"));
            }

            String platform = detectPlatform(url);
            String mediaId = extractMediaId(url, platform);

            if ("UNKNOWN".equals(platform)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "Unsupported or invalid platform URL. Only Instagram and YouTube are supported."));
            }

            String sql = "INSERT INTO ingested_media_links (partner_id, platform, original_url, media_id, status) VALUES (?, ?, ?, ?, 'PENDING_REVIEW')";
            jdbcTemplate.update(sql, userId, platform, url, mediaId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Link successfully ingested and queued for review.",
                    "platform", platform,
                    "mediaId", mediaId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // 2. Fetch Ingested Links Queue for Admins
    @GetMapping("/queue")
    public ResponseEntity<?> getIngestionQueue(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            String sql = "SELECT i.id, i.partner_id, u.email as partner_email, i.platform, i.original_url, i.media_id, i.status, i.submitted_at " +
                    "FROM ingested_media_links i JOIN users u ON i.partner_id = u.id ORDER BY i.submitted_at DESC";

            List<Map<String, Object>> queue = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(queue);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // 3. Update Ingestion Link Status (Approve/Reject)
    @PostMapping("/queue/{id}/status")
    public ResponseEntity<?> updateIngestionStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, @RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            String newStatus = payload.get("status"); // APPROVED or REJECTED
            if (newStatus == null || (!newStatus.equals("APPROVED") && !newStatus.equals("REJECTED"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "Invalid status value"));
            }

            String sql = "UPDATE ingested_media_links SET status = ? WHERE id = ?";
            jdbcTemplate.update(sql, newStatus, id);

            return ResponseEntity.ok(Map.of("success", true, "message", "Ingestion link status updated to " + newStatus));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String detectPlatform(String url) {
        if (url.contains("instagram.com")) return "INSTAGRAM";
        if (url.contains("youtube.com") || url.contains("youtu.be")) return "YOUTUBE";
        return "UNKNOWN";
    }

    private String extractMediaId(String url, String platform) {
        if ("INSTAGRAM".equals(platform)) {
            Pattern pattern = Pattern.compile("/(reel|p)/([A-Za-z0-9_-]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) return matcher.group(2);
        } else if ("YOUTUBE".equals(platform)) {
            Pattern pattern = Pattern.compile("(?:shorts/|watch\\?v=|youtu\\.be/)([A-Za-z0-9_-]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) return matcher.group(1);
        }
        return "EXTRACTED_GENERIC_ID";
    }
}