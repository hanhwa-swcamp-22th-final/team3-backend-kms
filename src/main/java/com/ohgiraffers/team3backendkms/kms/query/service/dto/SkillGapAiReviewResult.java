package com.ohgiraffers.team3backendkms.kms.query.service.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillGapAiReviewResult {

    private boolean aiEnabled;
    private String summary;
    private Map<String, String> gapRecommendations;

    public static SkillGapAiReviewResult disabled() {
        return SkillGapAiReviewResult.builder()
                .aiEnabled(false)
                .gapRecommendations(Map.of())
                .build();
    }
}
