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
@RequestMapping("/api/activity")
@CrossOrigin(origins = "http://localhost:5173")
public class UserActivityController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @GetMapping("/logs")
    public ResponseEntity<?> getUserActivityLogs(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String role = (String) claims.get("role");

            if (!"SUPER_ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Super Admin only.");
            }

            String sql = "SELECT l.id, l.admin_id, u.email as admin_email, l.action, l.payload, l.created_at " +
                    "FROM audit_logs l JOIN users u ON l.admin_id = u.id ORDER BY l.created_at DESC";

            List<Map<String, Object>> logs = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(Map.of("success", true, "logs", logs));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
}