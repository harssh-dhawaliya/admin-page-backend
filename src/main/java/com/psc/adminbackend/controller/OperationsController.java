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
@RequestMapping("/api/operations")
@CrossOrigin(origins = "http://localhost:5173")
public class OperationsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 1. Fetch Live Traveler Operations Monitor Data
    @GetMapping("/live-trips")
    public ResponseEntity<?> getLiveTrips(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            String sql = "SELECT t.trip_id, t.user_id, u.email as user_email, t.current_status, t.latitude, t.longitude, t.updated_at " +
                    "FROM active_trips t JOIN users u ON t.user_id = u.id ORDER BY t.updated_at DESC";

            List<Map<String, Object>> activeTrips = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(activeTrips);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // 2. Fetch Support Desk Tickets
    @GetMapping("/support-tickets")
    public ResponseEntity<?> getSupportTickets(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            String sql = "SELECT s.ticket_id, s.user_id, u.email as user_email, s.subject, s.channel, s.status, s.priority, s.created_at " +
                    "FROM support_tickets s JOIN users u ON s.user_id = u.id ORDER BY s.created_at DESC";

            List<Map<String, Object>> tickets = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(tickets);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
}