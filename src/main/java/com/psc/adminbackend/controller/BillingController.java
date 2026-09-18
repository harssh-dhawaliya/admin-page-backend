package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.SlackAlertService; // 1. Import the service
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "http://localhost:5173")
public class BillingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SlackAlertService slackAlertService; // 2. Inject the Slack Alert Service

    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 1. Fetch Financial Overview & Commissions Ledger
    @GetMapping("/ledger")
    public ResponseEntity<?> getFinancialLedger(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            String txSql = "SELECT t.transaction_id, t.partner_id, u.email as partner_email, t.booking_id, t.total_amount, t.platform_commission, t.partner_payout, t.payout_status, t.created_at " +
                    "FROM partner_transactions t JOIN users u ON t.partner_id = u.id ORDER BY t.created_at DESC";

            List<Map<String, Object>> transactions = jdbcTemplate.queryForList(txSql);

            Map<String, Object> totals = jdbcTemplate.queryForMap(
                    "SELECT COALESCE(SUM(total_amount), 0.00) as total_volume, " +
                            "COALESCE(SUM(platform_commission), 0.00) as total_revenue, " +
                            "COALESCE(SUM(partner_payout), 0.00) as total_payouts FROM partner_transactions"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "metrics", totals,
                    "transactions", transactions
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // 2. Record a Booking Transaction & Auto-Calculate Commission (10% Platform Rake)
    @PostMapping("/record-transaction")
    public ResponseEntity<?> recordTransaction(@RequestBody Map<String, Object> payload, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        try {
            Long partnerId = Long.valueOf(payload.get("partnerId").toString());
            String bookingId = (String) payload.get("bookingId");
            BigDecimal totalAmount = new BigDecimal(payload.get("amount").toString());

            BigDecimal commissionRate = new BigDecimal("0.10");
            BigDecimal platformCommission = totalAmount.multiply(commissionRate);
            BigDecimal partnerPayout = totalAmount.subtract(platformCommission);

            String sql = "INSERT INTO partner_transactions (partner_id, booking_id, total_amount, platform_commission, partner_payout, payout_status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
            jdbcTemplate.update(sql, partnerId, bookingId, totalAmount, platformCommission, partnerPayout);

            // 3. Trigger Slack Notification Alert
            slackAlertService.sendAlert(
                    "New Booking Transaction Recorded",
                    "Partner ID " + partnerId + " recorded booking " + bookingId + " for $" + totalAmount + " (Platform Rake: $" + platformCommission + ")",
                    "FINANCIAL"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction recorded and commission calculated successfully.",
                    "platformCommission", platformCommission,
                    "partnerPayout", partnerPayout
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}