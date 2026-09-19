package com.psc.adminbackend.controller;

import com.psc.adminbackend.service.AuditService;
import com.psc.adminbackend.service.LocalizationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService; // Injected Audit Service

    @Autowired
    private LocalizationService localizationService; // Injected Localization Service for SCRUM-35

    // Use a strong secret key for signing the JWT (In production, this goes in application.properties)
    private final String SECRET = "MySuperSecretKeyThatIsAtLeast32BytesLongForSecurity!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return jdbcTemplate.queryForList("SELECT id, email, role, kyc_status, language_preference FROM users");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String role = payload.getOrDefault("role", "SUPER_ADMIN");

        String hashedPassword = passwordEncoder.encode(password);
        String sql = "INSERT INTO users (email, password_hash, role, kyc_status, language_preference) VALUES (?, ?, ?, 'VERIFIED', 'en')";

        try {
            jdbcTemplate.update(sql, email, hashedPassword, role);

            // Log this action to our immutable audit log engine (admin_id 1 used as system/super-admin default fallback)
            auditService.logAction(1L, "CREATE_USER", "{\"target_email\":\"" + email + "\", \"role\":\"" + role + "\"}");

            return ResponseEntity.ok(Map.of("success", true, "message", "User created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Login Route
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String rawPassword = payload.get("password");

        try {
            // 1. Fetch user from database
            String sql = "SELECT id, password_hash, role, language_preference FROM users WHERE email = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, email);

            String dbPasswordHash = (String) user.get("password_hash");

            // 2. Check if password matches
            if (passwordEncoder.matches(rawPassword, dbPasswordHash)) {

                // 3. Generate JWT Token (Expires in 24 hours)
                String token = Jwts.builder()
                        .subject(email)
                        .claim("id", user.get("id"))
                        .claim("role", user.get("role"))
                        .claim("languagePreference", user.get("language_preference"))
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 86400000))
                        .signWith(key)
                        .compact();

                // 4. Return the token
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("token", token);
                response.put("role", user.get("role"));
                response.put("languagePreference", user.get("language_preference"));

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (EmptyResultDataAccessException e) {
            // User not found in database
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // Language Preference Update Route (SCRUM-35)
    @PutMapping("/users/{userId}/language")
    public ResponseEntity<?> updateLanguagePreference(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String lang = payload.getOrDefault("languagePreference", "en");
        String sql = "UPDATE users SET language_preference = ? WHERE id = ?";

        try {
            int updated = jdbcTemplate.update(sql, lang, userId);
            if (updated > 0) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Language preference updated to " + lang));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Localized Notification Testing Route (SCRUM-35)
    @GetMapping("/users/{userId}/notification-test")
    public ResponseEntity<?> getLocalizedNotification(@PathVariable Long userId, @RequestParam(defaultValue = "en") String lang) {
        String welcomeMessage = localizationService.getLocalizedMessage("notification.welcome", lang);
        String confirmationMessage = localizationService.getLocalizedMessage("notification.booking.confirmed", lang);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "language", lang,
                "notifications", Map.of(
                        "welcome", welcomeMessage,
                        "bookingConfirmed", confirmationMessage
                )
        ));
    }
}