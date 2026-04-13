package com.ohgiraffers.team3backendkms.kms.command.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeArticleViewGuardService {

    private final Map<String, LocalDateTime> recentViews = new ConcurrentHashMap<>();
    private final Duration duplicateWindow;

    public KnowledgeArticleViewGuardService(
            @Value("${kms.view-count.duplicate-window-minutes:1440}") long duplicateWindowMinutes
    ) {
        this.duplicateWindow = Duration.ofMinutes(Math.max(1, duplicateWindowMinutes));
    }

    public boolean shouldIncrease(Long articleId, Long requesterId) {
        if (articleId == null) {
            return false;
        }

        if (requesterId == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minus(duplicateWindow);

        recentViews.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        String key = requesterId + ":" + articleId;
        LocalDateTime previousViewedAt = recentViews.get(key);

        if (previousViewedAt != null && !previousViewedAt.isBefore(cutoff)) {
            return false;
        }

        recentViews.put(key, now);
        return true;
    }
}
