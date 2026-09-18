package com.psc.adminbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cms")
@CrossOrigin(origins = "http://localhost:5173")
public class SpotController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET: Fetch all cities
    @GetMapping("/cities")
    public List<Map<String, Object>> getCities() {
        return jdbcTemplate.queryForList("SELECT * FROM cities");
    }

    // POST: Add a new city
    @PostMapping("/cities")
    public String addCity(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String country = payload.get("country");
        String description = payload.get("description");

        String sql = "INSERT INTO cities (name, country, description) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, name, country, description);
        return "City added successfully!";
    }

    // GET: Fetch POIs for a specific city
    @GetMapping("/pois/{cityId}")
    public List<Map<String, Object>> getPoisByCity(@PathVariable Long cityId) {
        String sql = "SELECT * FROM pois WHERE city_id = ?";
        return jdbcTemplate.queryForList(sql, cityId);
    }

    // POST: Add a new POI
    @PostMapping("/pois")
    public String addPoi(@RequestBody Map<String, Object> payload) {
        Long cityId = Long.valueOf(payload.get("city_id").toString());
        String title = (String) payload.get("title");
        String category = (String) payload.get("category");
        String description = (String) payload.get("description");

        String sql = "INSERT INTO pois (city_id, title, category, description) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, cityId, title, category, description);
        return "POI added successfully!";
    }
}