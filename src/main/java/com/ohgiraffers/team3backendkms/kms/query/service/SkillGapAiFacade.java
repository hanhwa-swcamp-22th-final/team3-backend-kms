package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillGapAiFacade {

    private final SkillGapAiReviewService skillGapAiReviewService;

    @CircuitBreaker(name = "skill-gap-ai", fallbackMethod = "fallback")
    public SkillGapAiReviewResult review(SkillGapAiReviewRequest request) {
        return skillGapAiReviewService.review(request);
    }

    @SuppressWarnings("unused")
    private SkillGapAiReviewResult fallback(SkillGapAiReviewRequest request, Throwable throwable) {
        log.warn("[SkillGap AI] circuit breaker fallback - {}", throwable.getClass().getSimpleName(), throwable);
        return SkillGapAiReviewResult.disabled();
    }
}
