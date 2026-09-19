package com.psc.adminbackend.service;

import com.psc.adminbackend.entity.GroupMessage;
import com.psc.adminbackend.entity.TravelGroup;
import com.psc.adminbackend.repository.GroupMessageRepository;
import com.psc.adminbackend.repository.TravelGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityChatService {

    @Autowired
    private TravelGroupRepository groupRepository;

    @Autowired
    private GroupMessageRepository messageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TravelGroup createGroup(String groupName, Long creatorId) {
        TravelGroup group = new TravelGroup();
        group.setGroupName(groupName);
        group.setCreatorId(creatorId);
        return groupRepository.save(group);
    }

    @Transactional
    public GroupMessage sendMessage(Long groupId, Long userId, String messageText) {
        // 1. Check if user is already permanently blocked
        Long blockCount = entityManager.createQuery(
                        "SELECT COUNT(b) FROM Object b WHERE b.userId = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (blockCount > 0) {
            throw new SecurityException("Access denied. User is permanently blocked under zero-tolerance policy.");
        }

        // 2. Run Zero-Tolerance Moderation Check
        boolean passesModeration = runZeroToleranceCheck(messageText);

        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setUserId(userId);
        message.setMessageText(messageText);

        if (!passesModeration) {
            message.setStatus("BLOCKED_VIOLATION");
            // Execute no-warning permanent block rule
            entityManager.createNativeQuery("INSERT INTO blocked_users (user_id, reason) VALUES (?, ?)")
                    .setParameter(1, userId)
                    .setParameter(2, "Zero-tolerance policy violation in community chat message.")
                    .executeUpdate();

            messageRepository.save(message);
            throw new SecurityException("Message violated zero-tolerance guidelines. User has been permanently blocked.");
        }

        message.setStatus("APPROVED");
        return messageRepository.save(message);
    }

    public List<GroupMessage> getGroupMessages(Long groupId) {
        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
    }

    private boolean runZeroToleranceCheck(String text) {
        if (text == null) return true;
        String lower = text.toLowerCase();
        // Zero-tolerance filter keywords
        return !lower.contains("spam_token_bad") && !lower.contains("harass_term");
    }
}