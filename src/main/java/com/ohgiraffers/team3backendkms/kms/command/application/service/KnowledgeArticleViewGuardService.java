package com.ohgiraffers.team3backendkms.kms.command.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeArticleViewGuardService {

    // 서버 메모리에 저장해서 재시작시 기록 초기화됨
    private final Map<String, LocalDateTime> recentViews = new ConcurrentHashMap<>();
    private final Duration duplicateWindow;

    public KnowledgeArticleViewGuardService(
            @Value("${kms.view-count.duplicate-window-minutes:1440}") long duplicateWindowMinutes
    ) {
        this.duplicateWindow = Duration.ofMinutes(Math.max(1, duplicateWindowMinutes));
    }

    public boolean shouldIncrease(Long articleId, Long requesterId) {
        // 게시글 아이디가 없으면
        if (articleId == null) {
            return false;
        }

        // 사용자 아이디가 없으면  로그인 유무 확인
        if (requesterId == null) {
            return false;
        }

        // 현재시각 구하고 cutoff는 24시간전
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minus(duplicateWindow);

        // 메모리에 저장된 기록중 기준 시각보다 오래된 기록은 삭제
        // 중복체크에 해당안하는 데이터를 정리해서 메모리에 부담을 줄임
        recentViews.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        // 사용자 + 게시글 == key 해서 이전조회기록이 있는지 확인
        String key = requesterId + ":" + articleId;
        LocalDateTime previousViewedAt = recentViews.get(key);

        ///  중복 판단 하고 같은사용자면 증가안함
        if (previousViewedAt != null && !previousViewedAt.isBefore(cutoff)) {
            return false;
        }

        // 새기록저장
        recentViews.put(key, now);
        return true;
    }
}
