package com.psc.adminbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Execute raw SQL queries to get real-time counts
            Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            Integer pendingKyc = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE kyc_status = 'PENDING'", Integer.class);
            Integer totalPartners = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE role = 'PARTNER'", Integer.class);

            stats.put("success", true);
            stats.put("totalUsers", totalUsers);
            stats.put("pendingKyc", pendingKyc);
            stats.put("totalPartners", totalPartners);
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }
}