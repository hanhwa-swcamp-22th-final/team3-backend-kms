package com.ohgiraffers.team3backendkms.kms.command.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeArticleViewGuardService {

    // 같은 사용자/문서 조합의 마지막 조회 시각을 메모리에 저장해 중복 조회수를 막는다.
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

        // 중복 차단 구간 밖의 오래된 기록은 매 요청마다 가볍게 정리한다.
        recentViews.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        String key = requesterId + ":" + articleId;
        LocalDateTime previousViewedAt = recentViews.get(key);

        if (previousViewedAt != null && !previousViewedAt.isBefore(cutoff)) {
            return false;
        }

        // 차단 구간을 통과한 첫 조회만 현재 시각으로 갱신한다.
        recentViews.put(key, now);
        return true;
    }
}
