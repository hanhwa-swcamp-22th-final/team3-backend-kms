package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendkms.infrastructure.client.HrClient;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeProfileResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeSkillResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.HrTierCriteriaItem;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.WorkerSkillGapResponse;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WorkerSkillGapQueryServiceTest {

    @InjectMocks
    private WorkerSkillGapQueryService workerSkillGapQueryService;

    @Mock
    private AdminClient adminClient;

    @Mock
    private HrClient hrClient;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Mock
    private SkillGapAiReviewService skillGapAiReviewService;

    @Nested
    @DisplayName("getSkillGap()")
    class GetSkillGap {

        @Test
        @DisplayName("Applies AI-generated summary and recommendations when AI review succeeds")
        void appliesAiReview() {
            // given
            givenBaseResponses();
            given(skillGapAiReviewService.review(any()))
                    .willReturn(SkillGapAiReviewResult.builder()
                            .aiEnabled(true)
                            .summary("AI가 생성한 전체 스킬 갭 요약입니다.")
                            .gapRecommendations(Map.of(
                                    "설비대응", "AI가 설비대응 보강 우선순위를 높게 제안했습니다.",
                                    "안전준수", "AI가 안전준수 실습과 문서 학습 병행을 제안했습니다."
                            ))
                            .build());

            // when
            WorkerSkillGapResponse response = workerSkillGapQueryService.getSkillGap(1001L);

            // then
            assertEquals("AI가 생성한 전체 스킬 갭 요약입니다.", response.getReport().getSummary());
            assertEquals("AI가 설비대응 보강 우선순위를 높게 제안했습니다.",
                    response.getReport().getGaps().stream()
                            .filter(gap -> "설비대응".equals(gap.getSkillName()))
                            .findFirst()
                            .orElseThrow()
                            .getRecommendation());
            assertEquals("문서 제목", response.getArticles().get(0).getTitle());
        }

        @Test
        @DisplayName("Keeps rule-based summary and recommendations when AI review is unavailable")
        void keepsFallbackWhenAiUnavailable() {
            // given
            givenBaseResponses();
            given(skillGapAiReviewService.review(any()))
                    .willReturn(SkillGapAiReviewResult.disabled());

            // when
            WorkerSkillGapResponse response = workerSkillGapQueryService.getSkillGap(1001L);

            // then
            assertEquals("현재 역량 기준으로 다음 티어까지 평균 21점 차이가 있습니다. 점수가 부족한 역량부터 보강하세요.",
                    response.getReport().getSummary());
            assertEquals("설비대응 역량을 우선 보강하면 다음 티어 목표에 더 가깝게 접근할 수 있습니다.",
                    response.getReport().getGaps().stream()
                            .filter(gap -> "설비대응".equals(gap.getSkillName()))
                            .findFirst()
                            .orElseThrow()
                            .getRecommendation());
        }

        private void givenBaseResponses() {
            AdminEmployeeProfileResponse profile = new AdminEmployeeProfileResponse();
            profile.setEmployeeId(1001L);
            profile.setCurrentTier("B");

            AdminEmployeeSkillResponse equipment = new AdminEmployeeSkillResponse();
            equipment.setSkillName("EQUIPMENT_RESPONSE");
            equipment.setSkillScore(BigDecimal.valueOf(40));

            AdminEmployeeSkillResponse safety = new AdminEmployeeSkillResponse();
            safety.setSkillName("SAFETY_COMPLIANCE");
            safety.setSkillScore(BigDecimal.valueOf(55));

            AdminEmployeeSkillResponse quality = new AdminEmployeeSkillResponse();
            quality.setSkillName("QUALITY_MANAGEMENT");
            quality.setSkillScore(BigDecimal.valueOf(60));

            AdminEmployeeSkillResponse productivity = new AdminEmployeeSkillResponse();
            productivity.setSkillName("PRODUCTIVITY");
            productivity.setSkillScore(BigDecimal.valueOf(50));

            AdminEmployeeSkillResponse transfer = new AdminEmployeeSkillResponse();
            transfer.setSkillName("TECHNICAL_TRANSFER");
            transfer.setSkillScore(BigDecimal.valueOf(52));

            AdminEmployeeSkillResponse innovation = new AdminEmployeeSkillResponse();
            innovation.setSkillName("INNOVATION_PROPOSAL");
            innovation.setSkillScore(BigDecimal.valueOf(45));

            HrTierCriteriaItem targetCriteria = new HrTierCriteriaItem();
            targetCriteria.setTier("A");
            targetCriteria.setTierConfigPromotionPoint(70);
            targetCriteria.setEquipmentResponseTargetScore(70.0);
            targetCriteria.setTechnicalTransferTargetScore(70.0);
            targetCriteria.setInnovationProposalTargetScore(68.0);
            targetCriteria.setSafetyComplianceTargetScore(72.0);
            targetCriteria.setQualityManagementTargetScore(73.0);
            targetCriteria.setProductivityTargetScore(70.0);

            ArticleReadDto article = new ArticleReadDto();
            article.setArticleId(1L);
            article.setArticleTitle("문서 제목");
            article.setArticlePreview("미리보기");
            article.setAuthorTier("A");
            article.setViewCount(12);

            given(adminClient.getEmployeeProfile(1001L)).willReturn(profile);
            given(adminClient.getEmployeeSkills(1001L)).willReturn(List.of(
                    equipment, safety, quality, productivity, transfer, innovation
            ));
            given(hrClient.getTierCriteria()).willReturn(List.of(targetCriteria));
            given(knowledgeArticleMapper.findSkillGapRecommendations(anyMap())).willReturn(List.of(article));
        }
    }
}
