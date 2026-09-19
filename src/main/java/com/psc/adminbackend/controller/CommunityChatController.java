package com.psc.adminbackend.controller;

import com.psc.adminbackend.entity.GroupMessage;
import com.psc.adminbackend.entity.TravelGroup;
import com.psc.adminbackend.service.CommunityChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/community")
@CrossOrigin(origins = "http://localhost:5173")
public class CommunityChatController {

    @Autowired
    private CommunityChatService chatService;

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> payload) {
        String groupName = (String) payload.get("groupName");
        Long creatorId = Long.valueOf(payload.getOrDefault("creatorId", 1).toString());

        TravelGroup group = chatService.createGroup(groupName, creatorId);
        return ResponseEntity.ok(Map.of("success", true, "data", group));
    }

    @PostMapping("/groups/{groupId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long groupId, @RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.getOrDefault("userId", 1).toString());
            String messageText = (String) payload.get("messageText");

            GroupMessage msg = chatService.sendMessage(groupId, userId, messageText);
            return ResponseEntity.ok(Map.of("success", true, "data", msg));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", se.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long groupId) {
        List<GroupMessage> messages = chatService.getGroupMessages(groupId);
        return ResponseEntity.ok(Map.of("success", true, "data", messages));
    }
}