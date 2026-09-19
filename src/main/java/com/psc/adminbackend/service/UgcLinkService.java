package com.psc.adminbackend.service;

import com.psc.adminbackend.entity.UgcLink;
import com.psc.adminbackend.repository.UgcLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UgcLinkService {

    @Autowired
    private UgcLinkRepository ugcLinkRepository;

    public UgcLink submitUgcLink(Long userId, String destinationId, String url, String platform) {
        // Trigger zero-tolerance moderation check (SCRUM-5)
        boolean passesModeration = runZeroToleranceCheck(url);

        UgcLink link = new UgcLink();
        link.setUserId(userId);
        link.setDestinationId(destinationId);
        link.setUrl(url);
        link.setPlatform(platform);

        if (passesModeration) {
            link.setStatus("APPROVED");
        } else {
            link.setStatus("REJECTED_ZERO_TOLERANCE_VIOLATION");
        }

        return ugcLinkRepository.save(link);
    }

    private boolean runZeroToleranceCheck(String url) {
        String lowerUrl = url.toLowerCase();
        // Zero-tolerance policy check for flagged keywords or patterns
        if (lowerUrl.contains("spam") || lowerUrl.contains("malicious") || lowerUrl.contains("nsfw")) {
            return false;
        }
        return true;
    }
}