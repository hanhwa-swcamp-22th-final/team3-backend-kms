package com.ohgiraffers.team3backendkms.kms.query.service.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillGapAiReviewRequest {

    private String currentTier;
    private String targetTier;
    private Integer currentOverall;
    private Integer targetOverall;
    private Integer totalGap;
    private String confidence;
    private List<SkillSnapshot> skills;
    private List<GapSnapshot> topGaps;
    private List<String> relatedArticleTitles;

    @Getter
    @Builder
    public static class SkillSnapshot {
        private String label;
        private int current;
        private int target;
        private int gap;
    }

    @Getter
    @Builder
    public static class GapSnapshot {
        private String priority;
        private String skillName;
        private int current;
        private int target;
        private int gap;
        private String recommendation;
    }
}
