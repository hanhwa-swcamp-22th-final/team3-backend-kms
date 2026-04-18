package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendkms.infrastructure.client.HrClient;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeProfileResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeSkillResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.HrTierCriteriaItem;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.WorkerSkillGapResponse;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkerSkillGapQueryService {

    private static final String TOP_PRIORITY_COLOR = "#EF476F";
    private static final String MID_PRIORITY_COLOR = "#FFD166";
    private static final String LOW_PRIORITY_COLOR = "#5B4FCF";
    private static final int SKILL_COUNT = SkillType.values().length;
    private static final DecimalFormat CONFIDENCE_FORMAT = new DecimalFormat("0.00");

    private final AdminClient adminClient;
    private final HrClient hrClient;
    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final SkillGapAiReviewService skillGapAiReviewService;

    public WorkerSkillGapQueryService(
            AdminClient adminClient,
            HrClient hrClient,
            KnowledgeArticleMapper knowledgeArticleMapper,
            SkillGapAiReviewService skillGapAiReviewService
    ) {
        this.adminClient = adminClient;
        this.hrClient = hrClient;
        this.knowledgeArticleMapper = knowledgeArticleMapper;
        this.skillGapAiReviewService = skillGapAiReviewService;
    }

    public WorkerSkillGapResponse getSkillGap(Long employeeId) {
        AdminEmployeeProfileResponse profile = adminClient.getEmployeeProfile(employeeId);
        List<AdminEmployeeSkillResponse> rawSkills = adminClient.getEmployeeSkills(employeeId);
        Map<SkillType, Integer> currentScores = buildCurrentScores(rawSkills);

        String currentTier = resolveCurrentTier(profile, currentScores);
        String targetTier = resolveTargetTier(currentTier);
        HrTierCriteriaItem criteria = resolveCriteria(targetTier, hrClient.getTierCriteria());
        Map<SkillType, Integer> targetScores = buildTargetScores(currentScores, criteria, currentTier, targetTier);

        List<WorkerSkillGapResponse.SkillGapSkill> skills = Arrays.stream(SkillType.values())
                .map(type -> toSkillGapSkill(type, currentScores, targetScores))
                .toList();

        int currentOverall = average(skills.stream().map(WorkerSkillGapResponse.SkillGapSkill::getCurrent).toList());
        int targetOverall = average(skills.stream().map(WorkerSkillGapResponse.SkillGapSkill::getTarget).toList());
        int totalGap = Math.max(targetOverall - currentOverall, 0);
        SkillType weakestSkill = resolveWeakestSkill(currentScores);
        List<WorkerSkillGapResponse.RelatedArticle> relatedArticles = buildRelatedArticles(weakestSkill);
        WorkerSkillGapResponse.Report report = buildReport(skills, rawSkills, currentTier, targetTier, totalGap);
        report = applyAiReview(report, skills, currentTier, targetTier, currentOverall, targetOverall, totalGap, relatedArticles);

        return WorkerSkillGapResponse.builder()
                .currentTier(currentTier)
                .targetTier(targetTier)
                .skills(skills)
                .summary(WorkerSkillGapResponse.Summary.builder()
                        .currentOverall(currentOverall)
                        .targetOverall(targetOverall)
                        .totalGap(totalGap)
                        .build())
                .report(report)
                .courses(List.of())
                .articles(relatedArticles)
                .build();
    }

    private Map<SkillType, Integer> buildCurrentScores(List<AdminEmployeeSkillResponse> rawSkills) {
        Map<SkillType, Integer> currentScores = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            currentScores.put(type, 0);
        }

        for (AdminEmployeeSkillResponse rawSkill : rawSkills) {
            SkillType.fromCode(rawSkill.getSkillName())
                    .ifPresent(type -> currentScores.put(type, toInt(rawSkill.getSkillScore())));
        }
        return currentScores;
    }

    private HrTierCriteriaItem resolveCriteria(String targetTier, List<HrTierCriteriaItem> criteriaList) {
        return criteriaList.stream()
                .filter(item -> targetTier.equalsIgnoreCase(item.getTier()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("목표 티어 기준을 찾을 수 없습니다. targetTier=" + targetTier));
    }

    private Map<SkillType, Integer> buildTargetScores(
            Map<SkillType, Integer> currentScores,
            HrTierCriteriaItem criteria,
            String currentTier,
            String targetTier
    ) {
        Map<SkillType, Integer> targetScores = new EnumMap<>(SkillType.class);
        boolean highestTier = "S".equals(currentTier) && "S".equals(targetTier);

        for (SkillType type : SkillType.values()) {
            int current = currentScores.getOrDefault(type, 0);
            int target = highestTier ? current : type.resolveTarget(criteria);
            targetScores.put(type, Math.max(target, current));
        }
        return targetScores;
    }

    private WorkerSkillGapResponse.SkillGapSkill toSkillGapSkill(
            SkillType type,
            Map<SkillType, Integer> currentScores,
            Map<SkillType, Integer> targetScores
    ) {
        int current = currentScores.getOrDefault(type, 0);
        int target = targetScores.getOrDefault(type, current);
        return WorkerSkillGapResponse.SkillGapSkill.builder()
                .label(type.label)
                .current(current)
                .target(target)
                .gap(Math.max(target - current, 0))
                .build();
    }

    private WorkerSkillGapResponse.Report buildReport(
            List<WorkerSkillGapResponse.SkillGapSkill> skills,
            List<AdminEmployeeSkillResponse> rawSkills,
            String currentTier,
            String targetTier,
            int totalGap
    ) {
        boolean highestTier = "S".equals(currentTier) && "S".equals(targetTier);
        String summary = highestTier
                ? "현재 최고 티어(S)입니다. 현재 역량을 유지하고 핵심 역량 편차를 관리하세요."
                : "현재 역량 기준으로 다음 티어까지 평균 " + totalGap + "점 차이가 있습니다. 점수가 부족한 역량부터 보강하세요.";

        List<WorkerSkillGapResponse.GapItem> gaps = skills.stream()
                .sorted(Comparator
                        .comparingInt(WorkerSkillGapResponse.SkillGapSkill::getCurrent)
                        .thenComparing((left, right) -> Integer.compare(right.getGap(), left.getGap()))
                        .thenComparingInt(skill -> SkillType.fromLabel(skill.getLabel())
                                .map(type -> type.priority)
                                .orElse(SKILL_COUNT)))
                .limit(3)
                .map(skill -> toGapItem(skill, highestTier))
                .toList();

        return WorkerSkillGapResponse.Report.builder()
                .summary(summary)
                .confidence(calculateConfidence(rawSkills, skills))
                .gaps(gaps)
                .prediction(WorkerSkillGapResponse.Prediction.builder()
                        .normalDate(highestTier ? "승급 대상 아님" : "추후 연동 예정")
                        .acceleratedDate(highestTier ? "승급 대상 아님" : "추후 연동 예정")
                        .build())
                .build();
    }

    private WorkerSkillGapResponse.GapItem toGapItem(
            WorkerSkillGapResponse.SkillGapSkill skill,
            boolean highestTier
    ) {
        GapPriority priority = GapPriority.fromGap(skill.getGap());
        String recommendation = highestTier
                ? skill.getLabel() + " 역량 유지 및 심화 학습을 권장합니다."
                : skill.getLabel() + " 역량을 우선 보강하면 다음 티어 목표에 더 가깝게 접근할 수 있습니다.";

        return WorkerSkillGapResponse.GapItem.builder()
                .id((long) skill.getLabel().hashCode())
                .priority(priority.label)
                .priorityIcon(priority.icon)
                .color(priority.color)
                .skillName(skill.getLabel())
                .current(skill.getCurrent())
                .target(skill.getTarget())
                .gap(skill.getGap())
                .recommendation(recommendation)
                .build();
    }

    private String calculateConfidence(
            List<AdminEmployeeSkillResponse> rawSkills,
            List<WorkerSkillGapResponse.SkillGapSkill> skills
    ) {
        long mappedSkillCount = rawSkills.stream()
                .map(AdminEmployeeSkillResponse::getSkillName)
                .map(SkillType::fromCode)
                .filter(Optional::isPresent)
                .count();
        long nonZeroSkillCount = skills.stream()
                .filter(skill -> skill.getCurrent() > 0)
                .count();
        double averageScoreRatio = skills.isEmpty()
                ? 0
                : skills.stream()
                .mapToInt(WorkerSkillGapResponse.SkillGapSkill::getCurrent)
                .average()
                .orElse(0) / 100.0;

        double confidence = 0.20
                + (Math.min(mappedSkillCount, SKILL_COUNT) / (double) SKILL_COUNT) * 0.25
                + (nonZeroSkillCount / (double) SKILL_COUNT) * 0.25
                + averageScoreRatio * 0.30;

        return CONFIDENCE_FORMAT.format(Math.min(1.0, confidence));
    }

    private WorkerSkillGapResponse.Report applyAiReview(
            WorkerSkillGapResponse.Report report,
            List<WorkerSkillGapResponse.SkillGapSkill> skills,
            String currentTier,
            String targetTier,
            int currentOverall,
            int targetOverall,
            int totalGap,
            List<WorkerSkillGapResponse.RelatedArticle> relatedArticles
    ) {
        SkillGapAiReviewResult aiReviewResult = skillGapAiReviewService.review(
                SkillGapAiReviewRequest.builder()
                        .currentTier(currentTier)
                        .targetTier(targetTier)
                        .currentOverall(currentOverall)
                        .targetOverall(targetOverall)
                        .totalGap(totalGap)
                        .confidence(report.getConfidence())
                        .skills(skills.stream()
                                .map(skill -> SkillGapAiReviewRequest.SkillSnapshot.builder()
                                        .label(skill.getLabel())
                                        .current(skill.getCurrent())
                                        .target(skill.getTarget())
                                        .gap(skill.getGap())
                                        .build())
                                .toList())
                        .topGaps(report.getGaps().stream()
                                .map(gap -> SkillGapAiReviewRequest.GapSnapshot.builder()
                                        .priority(gap.getPriority())
                                        .skillName(gap.getSkillName())
                                        .current(gap.getCurrent())
                                        .target(gap.getTarget())
                                        .gap(gap.getGap())
                                        .recommendation(gap.getRecommendation())
                                        .build())
                                .toList())
                        .relatedArticleTitles(relatedArticles.stream()
                                .map(WorkerSkillGapResponse.RelatedArticle::getTitle)
                                .toList())
                        .build()
        );

        if (!aiReviewResult.isAiEnabled()) {
            return report;
        }

        String summary = StringUtils.hasText(aiReviewResult.getSummary())
                ? aiReviewResult.getSummary()
                : report.getSummary();
        Map<String, String> recommendationBySkill = aiReviewResult.getGapRecommendations();

        List<WorkerSkillGapResponse.GapItem> gaps = report.getGaps().stream()
                .map(gap -> WorkerSkillGapResponse.GapItem.builder()
                        .id(gap.getId())
                        .priority(gap.getPriority())
                        .priorityIcon(gap.getPriorityIcon())
                        .color(gap.getColor())
                        .skillName(gap.getSkillName())
                        .current(gap.getCurrent())
                        .target(gap.getTarget())
                        .gap(gap.getGap())
                        .recommendation(resolveAiRecommendation(gap, recommendationBySkill))
                        .build())
                .toList();

        return WorkerSkillGapResponse.Report.builder()
                .summary(summary)
                .confidence(report.getConfidence())
                .gaps(gaps)
                .prediction(report.getPrediction())
                .build();
    }

    private String resolveAiRecommendation(
            WorkerSkillGapResponse.GapItem gap,
            Map<String, String> recommendationBySkill
    ) {
        String aiRecommendation = recommendationBySkill.get(gap.getSkillName());
        if (StringUtils.hasText(aiRecommendation)) {
            return aiRecommendation;
        }
        return gap.getRecommendation();
    }

    private int average(List<Integer> values) {
        return values.isEmpty()
                ? 0
                : (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private int toInt(BigDecimal value) {
        return value == null ? 0 : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private String resolveCurrentTier(
            AdminEmployeeProfileResponse profile,
            Map<SkillType, Integer> currentScores
    ) {
        // 점수가 전부 0이면 신입/미평가 상태로 보고 최저 티어(C) 기준으로 스킬갭을 계산한다.
        if (currentScores.values().stream().allMatch(score -> score == null || score == 0)) {
            return "C";
        }
        return normalizeTier(profile != null ? profile.getCurrentTier() : null);
    }

    private String normalizeTier(String tier) {
        if (tier == null || tier.isBlank()) {
            return "C";
        }
        return tier.trim().toUpperCase(Locale.ROOT);
    }

    private SkillType resolveWeakestSkill(Map<SkillType, Integer> currentScores) {
        return currentScores.entrySet().stream()
                .min(Comparator
                        .comparingInt((Map.Entry<SkillType, Integer> entry) -> entry.getValue() == null ? 0 : entry.getValue())
                        .thenComparingInt(entry -> entry.getKey().priority))
                .map(Map.Entry::getKey)
                .orElse(SkillType.EQUIPMENT_RESPONSE);
    }

    private List<WorkerSkillGapResponse.RelatedArticle> buildRelatedArticles(SkillType weakestSkill) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("categories", weakestSkill.categories);
        params.put("tagKeywords", weakestSkill.tagKeywords);
        params.put("limit", 3);

        List<ArticleReadDto> recommended = knowledgeArticleMapper.findSkillGapRecommendations(params);
        if (recommended.isEmpty()) {
            recommended = knowledgeArticleMapper.findRecommendations();
        }

        return recommended.stream()
                .limit(3)
                .map(article -> WorkerSkillGapResponse.RelatedArticle.builder()
                        .id(article.getArticleId())
                        .title(article.getArticleTitle())
                        .preview(article.getArticlePreview())
                        .authorTier(article.getAuthorTier())
                        // 프론트 기존 카드 shape를 유지하기 위해 조회수를 표시용 숫자로 재사용한다.
                        .likes(article.getViewCount() == null ? 0 : article.getViewCount())
                        .build())
                .toList();
    }

    private String resolveTargetTier(String currentTier) {
        return switch (currentTier) {
            case "C" -> "B";
            case "B" -> "A";
            case "A" -> "S";
            case "S" -> "S";
            default -> "B";
        };
    }

    private enum GapPriority {
        TOP("최우선", "!", TOP_PRIORITY_COLOR),
        MID("중요", "*", MID_PRIORITY_COLOR),
        LOW("권장", "+", LOW_PRIORITY_COLOR);

        private final String label;
        private final String icon;
        private final String color;

        GapPriority(String label, String icon, String color) {
            this.label = label;
            this.icon = icon;
            this.color = color;
        }

        private static GapPriority fromGap(int gap) {
            if (gap >= 20) {
                return TOP;
            }
            if (gap >= 10) {
                return MID;
            }
            return LOW;
        }
    }

    private enum SkillType {
        EQUIPMENT_RESPONSE(
                "EQUIPMENT_RESPONSE",
                "설비대응",
                1,
                List.of("설비대응", "설비 대응", "설비점검", "설비 점검", "설비운영"),
                List.of("EQUIPMENT_OPERATION", "TROUBLESHOOTING")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getEquipmentResponseTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        },
        TECHNICAL_TRANSFER(
                "TECHNICAL_TRANSFER",
                "기술전수",
                5,
                List.of("기술전수", "기술 전수", "작업표준", "작업 표준", "노하우"),
                List.of("PROCESS_IMPROVEMENT", "ETC")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getTechnicalTransferTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        },
        INNOVATION_PROPOSAL(
                "INNOVATION_PROPOSAL",
                "혁신제안",
                6,
                List.of("혁신제안", "혁신 제안", "혁신", "개선사례", "개선 사례"),
                List.of("PROCESS_IMPROVEMENT", "ETC")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getInnovationProposalTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        },
        SAFETY_COMPLIANCE(
                "SAFETY_COMPLIANCE",
                "안전준수",
                2,
                List.of("안전준수", "안전 준수", "안전", "안전수칙", "안전 수칙"),
                List.of("SAFETY")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getSafetyComplianceTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        },
        QUALITY_MANAGEMENT(
                "QUALITY_MANAGEMENT",
                "품질관리",
                3,
                List.of("품질관리", "품질 관리", "품질", "불량개선", "불량 개선"),
                List.of("PROCESS_IMPROVEMENT", "ETC")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getQualityManagementTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        },
        PRODUCTIVITY(
                "PRODUCTIVITY",
                "생산성",
                4,
                List.of("생산성", "공정개선", "공정 개선", "작업효율", "작업 효율"),
                List.of("PROCESS_IMPROVEMENT", "EQUIPMENT_OPERATION")
        ) {
            @Override
            int resolveTarget(HrTierCriteriaItem criteria) {
                return toInt(criteria.getProductivityTargetScore(), criteria.getTierConfigPromotionPoint());
            }
        };

        private final String code;
        private final String label;
        private final int priority;
        private final List<String> tagKeywords;
        private final List<String> categories;

        SkillType(
                String code,
                String label,
                int priority,
                List<String> tagKeywords,
                List<String> categories
        ) {
            this.code = code;
            this.label = label;
            this.priority = priority;
            this.tagKeywords = tagKeywords;
            this.categories = categories;
        }

        abstract int resolveTarget(HrTierCriteriaItem criteria);

        private static Optional<SkillType> fromCode(String code) {
            return Arrays.stream(values())
                    .filter(type -> type.code.equalsIgnoreCase(code))
                    .findFirst();
        }

        private static Optional<SkillType> fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(type -> type.label.equals(label))
                    .findFirst();
        }

        private static int toInt(Double value, Integer fallback) {
            if (value != null) {
                return (int) Math.round(value);
            }
            return fallback == null ? 0 : fallback;
        }
    }
}
