package com.psc.adminbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Method to write an immutable audit log entry
    public void logAction(Long adminId, String action, String payloadJson) {
        String sql = "INSERT INTO audit_logs (admin_id, action, payload) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, adminId, action, payloadJson);
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }
}