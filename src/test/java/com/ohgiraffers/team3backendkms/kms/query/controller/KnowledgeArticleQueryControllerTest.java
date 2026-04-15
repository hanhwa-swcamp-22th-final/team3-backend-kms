package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.PendingArticleQueryService;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = KnowledgeArticleQueryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import({GlobalExceptionHandler.class, KnowledgeArticleQueryControllerTest.SecurityTestConfig.class})
class KnowledgeArticleQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeArticleQueryService knowledgeArticleQueryService;

    @MockitoBean
    private PendingArticleQueryService pendingArticleQueryService;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    private EmployeeUserDetails authenticatedUser() {
        return new EmployeeUserDetails(
                10L,
                "EMP0010",
                List.of(new SimpleGrantedAuthority("WORKER"))
        );
    }

    @TestConfiguration
    static class SecurityTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Nested
    @DisplayName("GET /api/kms/articles")
    class GetArticles {

        @Test
        @DisplayName("Returns 200 OK with list JSON")
        void getArticles_success() throws Exception {
            // given
            ArticleReadDto dto = new ArticleReadDto();
            dto.setArticleId(1L);
            dto.setAuthorId(10L);
            dto.setAuthorName("홍길동");
            dto.setArticleTitle("테스트 제목입니다");
            dto.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto.setArticleStatus(ArticleStatus.APPROVED);
            dto.setViewCount(3);
            dto.setCreatedAt(LocalDateTime.of(2026, 3, 1, 12, 0));
            given(knowledgeArticleQueryService.getArticles(any(ArticleQueryRequest.class)))
                    .willReturn(List.of(dto));

            // when & then
            mockMvc.perform(get("/api/kms/articles").with(user(authenticatedUser())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("테스트 제목입니다"))
                    .andExpect(jsonPath("$.data[0].authorName").value("홍길동"));
        }

        @Test
        @DisplayName("Binds query parameters correctly")
        void getArticles_withQueryParams_success() throws Exception {
            // given
            given(knowledgeArticleQueryService.getArticles(any(ArticleQueryRequest.class)))
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/kms/articles")
                            .with(user(authenticatedUser()))
                            .param("category", "TROUBLESHOOTING")
                            .param("articleStatus", "APPROVED")
                            .param("searchType", "articleTitle")
                            .param("keyword", "테스트")
                            .param("sort", "latest")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Returns pending list when status=pending")
        void getArticles_withPendingStatus_success() throws Exception {
            // given
            PendingArticleDto dto = new PendingArticleDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 대기 문서 제목입니다");
            given(pendingArticleQueryService.getPendingArticles(any()))
                    .willReturn(List.of(dto));

            // when & then
            mockMvc.perform(get("/api/kms/articles")
                            .with(user(new EmployeeUserDetails(
                                    20L,
                                    "TL001",
                                    List.of(new SimpleGrantedAuthority("TL"))
                            )))
                            .param("status", "pending")
                            .param("keyword", "대기"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("승인 대기 문서 제목입니다"));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/articles/{articleId}")
    class GetArticleDetail {

        @Test
        @DisplayName("Returns 200 OK with detail JSON")
        void getArticleDetail_success() throws Exception {
            // given
            ArticleDetailDto dto = new ArticleDetailDto();
            dto.setArticleId(1L);
            dto.setAuthorId(10L);
            dto.setAuthorName("홍길동");
            dto.setArticleTitle("테스트 제목입니다");
            dto.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto.setArticleContent("본문 내용입니다.");
            dto.setArticleStatus(ArticleStatus.APPROVED);
            dto.setArticleApprovalOpinion("승인 의견");
            dto.setViewCount(5);
            dto.setCreatedAt(LocalDateTime.of(2026, 3, 1, 12, 0));
            dto.setUpdatedAt(LocalDateTime.of(2026, 3, 2, 9, 0));
            given(knowledgeArticleQueryService.getArticleDetail(1L, 10L)).willReturn(dto);

            // when & then
            mockMvc.perform(get("/api/kms/articles/1")
                            .with(user(authenticatedUser())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleId").value(1))
                    .andExpect(jsonPath("$.data.articleTitle").value("테스트 제목입니다"))
                    .andExpect(jsonPath("$.data.articleContent").value("본문 내용입니다."));
        }

        @Test
        @DisplayName("Returns 404 when article not found")
        void getArticleDetail_whenNotFound_thenNotFound() throws Exception {
            // given
            given(knowledgeArticleQueryService.getArticleDetail(999L, 10L))
                    .willThrow(new ResourceNotFoundException("문서를 찾을 수 없습니다. id=999"));

            // when & then
            mockMvc.perform(get("/api/kms/articles/999")
                            .with(user(authenticatedUser())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Returns pending detail when status=pending")
        void getArticleDetail_withPendingStatus_success() throws Exception {
            // given
            PendingArticleDetailDto dto = new PendingArticleDetailDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 상세 제목입니다");
            given(pendingArticleQueryService.getPendingArticleById(1L, 20L)).willReturn(dto);

            // when & then
            mockMvc.perform(get("/api/kms/articles/1")
                            .with(user(new EmployeeUserDetails(
                                    20L,
                                    "TL001",
                                    List.of(new SimpleGrantedAuthority("TL"))
                            )))
                            .param("status", "pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleId").value(1))
                    .andExpect(jsonPath("$.data.articleTitle").value("승인 상세 제목입니다"));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/stats")
    class GetStats {

        @Test
        @DisplayName("Returns pending stats when status=pending")
        void getStats_withPendingStatus_success() throws Exception {
            // given
            PendingArticleStatsDto dto = new PendingArticleStatsDto();
            dto.setPendingCount(5L);
            dto.setApprovedThisMonth(10L);
            dto.setRejectionRate(33.33);
            given(pendingArticleQueryService.getPendingStats(20L)).willReturn(dto);

            // when & then
            mockMvc.perform(get("/api/kms/stats")
                            .with(user(new EmployeeUserDetails(
                                    20L,
                                    "TL001",
                                    List.of(new SimpleGrantedAuthority("TL"))
                            )))
                            .param("status", "pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.pendingCount").value(5))
                    .andExpect(jsonPath("$.data.approvedThisMonth").value(10))
                    .andExpect(jsonPath("$.data.rejectionRate").value(33.33));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/articles/contributors")
    class GetTopContributors {

        @Test
        @DisplayName("Returns 200 OK with contributors list")
        void getTopContributors_success() throws Exception {
            // given
            ContributorRankDto dto1 = new ContributorRankDto();
            dto1.setEmployeeId(1L);
            dto1.setEmployeeName("홍길동");
            dto1.setArticleCount(5L);
            dto1.setRank(1);

            ContributorRankDto dto2 = new ContributorRankDto();
            dto2.setEmployeeId(2L);
            dto2.setEmployeeName("김영희");
            dto2.setArticleCount(3L);
            dto2.setRank(2);

            given(knowledgeArticleQueryService.getTopContributors(3))
                    .willReturn(List.of(dto1, dto2));

            // when & then
            mockMvc.perform(get("/api/kms/articles/contributors")
                            .param("limit", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].employeeName").value("홍길동"))
                    .andExpect(jsonPath("$.data[0].articleCount").value(5))
                    .andExpect(jsonPath("$.data[0].rank").value(1))
                    .andExpect(jsonPath("$.data[1].employeeName").value("김영희"))
                    .andExpect(jsonPath("$.data[1].articleCount").value(3));
        }

        @Test
        @DisplayName("Uses default limit of 3 when not specified")
        void getTopContributors_withDefaultLimit() throws Exception {
            // given
            given(knowledgeArticleQueryService.getTopContributors(3))
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/kms/articles/contributors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/kms/articles/recommendations")
    class GetRecommendations {

        @Test
        @DisplayName("Returns 200 OK with recommendations list")
        void getRecommendations_success() throws Exception {
            // given
            ArticleReadDto dto1 = new ArticleReadDto();
            dto1.setArticleId(1L);
            dto1.setAuthorId(10L);
            dto1.setAuthorName("홍길동");
            dto1.setArticleTitle("인기 있는 문서 1");
            dto1.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto1.setArticleStatus(ArticleStatus.APPROVED);
            dto1.setViewCount(100);
            dto1.setCreatedAt(LocalDateTime.of(2026, 3, 1, 12, 0));

            ArticleReadDto dto2 = new ArticleReadDto();
            dto2.setArticleId(2L);
            dto2.setAuthorId(11L);
            dto2.setAuthorName("김영희");
            dto2.setArticleTitle("인기 있는 문서 2");
            dto2.setArticleCategory(ArticleCategory.PROCESS_IMPROVEMENT);
            dto2.setArticleStatus(ArticleStatus.APPROVED);
            dto2.setViewCount(50);
            dto2.setCreatedAt(LocalDateTime.of(2026, 2, 15, 9, 0));

            given(knowledgeArticleQueryService.getRecommendations())
                    .willReturn(List.of(dto1, dto2));

            // when & then
            mockMvc.perform(get("/api/kms/articles/recommendations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("인기 있는 문서 1"))
                    .andExpect(jsonPath("$.data[0].viewCount").value(100))
                    .andExpect(jsonPath("$.data[1].articleId").value(2))
                    .andExpect(jsonPath("$.data[1].viewCount").value(50));
        }

        @Test
        @DisplayName("Returns empty list when no recommendations")
        void getRecommendations_whenNoData() throws Exception {
            // given
            given(knowledgeArticleQueryService.getRecommendations())
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/kms/articles/recommendations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
