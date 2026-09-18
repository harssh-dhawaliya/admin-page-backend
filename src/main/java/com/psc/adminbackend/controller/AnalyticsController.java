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
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @GetMapping("/overview")
    public ResponseEntity<?> getAnalyticsOverview(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            // Aggregate Financial Analytics
            Map<String, Object> financialMetrics = jdbcTemplate.queryForMap(
                    "SELECT COALESCE(SUM(total_amount), 0.00) as gross_volume, " +
                            "COALESCE(SUM(platform_commission), 0.00) as net_platform_revenue, " +
                            "COALESCE(SUM(partner_payout), 0.00) as total_payouts, " +
                            "COUNT(*) as total_transactions FROM partner_transactions"
            );

            // Aggregate Operational Counts
            Long totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
            Long totalActiveTrips = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM active_trips", Long.class);
            Long openTickets = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM support_tickets WHERE status = 'OPEN'", Long.class);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "financials", financialMetrics,
                    "operations", Map.of(
                            "totalUsers", totalUsers != null ? totalUsers : 0,
                            "activeTrips", totalActiveTrips != null ? totalActiveTrips : 0,
                            "openSupportTickets", openTickets != null ? openTickets : 0
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
}