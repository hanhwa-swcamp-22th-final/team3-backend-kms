package com.ohgiraffers.team3backendkms.kms.query.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkerSkillGapResponse {

    private String currentTier;
    private String targetTier;
    private List<SkillGapSkill> skills;
    private Summary summary;
    private Report report;
    private List<RecommendedCourse> courses;
    private List<RelatedArticle> articles;

    @Getter
    @Builder
    public static class SkillGapSkill {
        private String label;
        private int current;
        private int target;
        private int gap;
    }

    @Getter
    @Builder
    public static class Summary {
        private int currentOverall;
        private int targetOverall;
        private int totalGap;
    }

    @Getter
    @Builder
    public static class Report {
        private String summary;
        private String confidence;
        private List<GapItem> gaps;
        private Prediction prediction;
    }

    @Getter
    @Builder
    public static class GapItem {
        private Long id;
        private String priority;
        private String priorityIcon;
        private String color;
        private String skillName;
        private int current;
        private int target;
        private int gap;
        private String recommendation;
    }

    @Getter
    @Builder
    public static class Prediction {
        private String normalDate;
        private String acceleratedDate;
    }

    @Getter
    @Builder
    public static class RecommendedCourse {
        private Long id;
        private String priority;
        private String priorityColor;
        private String category;
        private String title;
        private String description;
        private String duration;
        private String durationDiff;
        private String status;
    }

    @Getter
    @Builder
    public static class RelatedArticle {
        private Long id;
        private String title;
        private int likes;
    }
}
