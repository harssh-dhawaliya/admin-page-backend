package com.psc.adminbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Live queries hitting your MySQL database tables
            Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

            response.put("success", true);
            response.put("user", Map.of(
                    "firstName", "Akshita",
                    "fullName", "Akshita Pandey",
                    "role", "Super Admin",
                    "initials", "AP"
            ));
            response.put("metrics", List.of(
                    Map.of("id", "active-users", "label", "Active Users", "value", totalUsers != null ? totalUsers : 24680, "tone", "blue", "icon", "group", "changePercent", 8.2, "sparkline", List.of(20, 25, 30, 28, 35, 40, 45)),
                    Map.of("id", "new-users", "label", "New Users", "value", 1842, "tone", "green", "icon", "userPlus", "changePercent", 12.5, "sparkline", List.of(10, 15, 18, 22, 30, 28, 42)),
                    Map.of("id", "total-users", "label", "Total Users", "value", totalUsers != null ? totalUsers : 52430, "tone", "purple", "icon", "users", "changePercent", 5.6, "sparkline", List.of(50, 52, 55, 58, 60, 63, 65)),
                    Map.of("id", "pending-kyc", "label", "Pending KYC", "value", 129, "tone", "amber", "icon", "approval", "changePercent", 2.1, "sparkline", List.of(5, 8, 6, 9, 11, 10, 14))
            ));
            response.put("reportingPeriod", Map.of(
                    "startDate", "2024-05-12",
                    "endDate", "2024-05-18",
                    "comparisonLabel", "vs last week"
            ));
            response.put("newUsersChart", Map.of(
                    "title", "New Users by Time",
                    "periods", Map.of(
                            "week", Map.of("label", "This Week", "points", List.of(
                                    Map.of("label", "May 12", "value", 50),
                                    Map.of("label", "May 13", "value", 115),
                                    Map.of("label", "May 14", "value", 160),
                                    Map.of("label", "May 15", "value", 78),
                                    Map.of("label", "May 16", "value", 180),
                                    Map.of("label", "May 17", "value", 240),
                                    Map.of("label", "May 18", "value", 212)
                            ))
                    )
            ));
            response.put("preferredCities", List.of(
                    Map.of("id", 1, "name", "Paris", "symbol", "P", "accent", "blue", "users", 12450, "sharePercent", 32.4),
                    Map.of("id", 2, "name", "Tokyo", "symbol", "T", "accent", "purple", "users", 9820, "sharePercent", 25.6),
                    Map.of("id", 3, "name", "New York", "symbol", "NY", "accent", "green", "users", 7640, "sharePercent", 19.9)
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}