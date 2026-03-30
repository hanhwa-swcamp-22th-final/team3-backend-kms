package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = KnowledgeArticleQueryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class KnowledgeArticleQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeArticleQueryService knowledgeArticleQueryService;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("GET /api/kms/articles")
    class GetArticles {

        @Test
        // 지식 목록 API 성공: 목록 JSON을 반환한다
        @DisplayName("Returns 200 OK with list JSON")
        void getArticles_success() throws Exception {
            // given
            ArticleReadDto dto = new ArticleReadDto(
                    1L, 10L, "홍길동", "테스트 제목입니다",
                    ArticleCategory.TROUBLESHOOTING, ArticleStatus.APPROVED,
                    3, LocalDateTime.of(2026, 3, 1, 12, 0)
            );
            given(knowledgeArticleQueryService.getArticles(any(ArticleQueryRequest.class)))
                    .willReturn(List.of(dto));

            // when & then
            mockMvc.perform(get("/api/kms/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("테스트 제목입니다"))
                    .andExpect(jsonPath("$.data[0].authorName").value("홍길동"));
        }

        @Test
        // 지식 목록 API 성공: 쿼리 파라미터가 정상 바인딩된다
        @DisplayName("Binds query parameters correctly")
        void getArticles_withQueryParams_success() throws Exception {
            // given
            given(knowledgeArticleQueryService.getArticles(any(ArticleQueryRequest.class)))
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/kms/articles")
                            .param("category", "TROUBLESHOOTING")
                            .param("status", "APPROVED")
                            .param("sort", "latest")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/kms/articles/{articleId}")
    class GetArticleDetail {

        @Test
        // 지식 상세 API 성공: 상세 JSON을 반환한다
        @DisplayName("Returns 200 OK with detail JSON")
        void getArticleDetail_success() throws Exception {
            // given
            ArticleDetailDto dto = new ArticleDetailDto(
                    1L, 10L, "홍길동", "테스트 제목입니다",
                    ArticleCategory.TROUBLESHOOTING, "본문 내용입니다.",
                    ArticleStatus.APPROVED, "승인 의견", null,
                    5, LocalDateTime.of(2026, 3, 1, 12, 0), LocalDateTime.of(2026, 3, 2, 9, 0)
            );
            given(knowledgeArticleQueryService.getArticleDetail(1L)).willReturn(dto);

            // when & then
            mockMvc.perform(get("/api/kms/articles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleId").value(1))
                    .andExpect(jsonPath("$.data.articleTitle").value("테스트 제목입니다"))
                    .andExpect(jsonPath("$.data.articleContent").value("본문 내용입니다."));
        }

        @Test
        // 지식 상세 API 실패: 문서가 없으면 404를 반환한다
        @DisplayName("Returns 404 when article not found")
        void getArticleDetail_whenNotFound_thenNotFound() throws Exception {
            // given
            given(knowledgeArticleQueryService.getArticleDetail(999L))
                    .willThrow(new ResourceNotFoundException("문서를 찾을 수 없습니다. id=999"));

            // when & then
            mockMvc.perform(get("/api/kms/articles/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
        }
    }
}
