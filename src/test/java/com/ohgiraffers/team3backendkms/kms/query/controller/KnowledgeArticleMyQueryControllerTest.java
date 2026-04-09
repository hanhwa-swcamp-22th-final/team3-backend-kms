package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleMyQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KnowledgeArticleMyQueryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class KnowledgeArticleMyQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeArticleMyQueryService knowledgeArticleMyQueryService;

    @Nested
    @DisplayName("GET /api/kms/my/articles/stats")
    class GetMyArticleStats {

        @Test
        @DisplayName("Returns 200 OK with stats")
        void getMyArticleStats_success() throws Exception {
            MyArticleStatsDto dto = new MyArticleStatsDto();
            dto.setApprovedCount(3L);
            dto.setPendingCount(1L);
            dto.setRejectedCount(2L);
            dto.setDraftCount(4L);
            given(knowledgeArticleMyQueryService.getMyArticleStats(10L)).willReturn(dto);

            mockMvc.perform(get("/api/kms/my/articles/stats").param("authorId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.approvedCount").value(3))
                    .andExpect(jsonPath("$.data.draftCount").value(4));
        }

        @Test
        @DisplayName("Returns 400 when authorId is missing")
        void getMyArticleStats_whenAuthorIdMissing_thenBadRequest() throws Exception {
            mockMvc.perform(get("/api/kms/my/articles/stats"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/kms/my/articles")
    class GetMyArticles {

        @Test
        @DisplayName("Returns 200 OK with article list and tags")
        void getMyArticles_success() throws Exception {
            MyArticleDto article = new MyArticleDto();
            article.setArticleId(1L);
            article.setArticleTitle("내 문서");
            article.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            article.setArticleStatus(ArticleStatus.APPROVED);
            article.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));

            KnowledgeTagReadDto tag = new KnowledgeTagReadDto();
            tag.setTagId(100L);
            tag.setTagName("가공");
            article.setTags(List.of(tag));

            given(knowledgeArticleMyQueryService.getMyArticles(any(MyArticleQueryRequest.class)))
                    .willReturn(List.of(article));

            mockMvc.perform(get("/api/kms/my/articles")
                            .param("authorId", "10")
                            .param("status", "APPROVED")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("내 문서"))
                    .andExpect(jsonPath("$.data[0].tags[0].tagId").value(100))
                    .andExpect(jsonPath("$.data[0].tags[0].tagName").value("가공"));
        }

        @Test
        @DisplayName("Returns 400 when authorId is missing")
        void getMyArticles_whenAuthorIdMissing_thenBadRequest() throws Exception {
            mockMvc.perform(get("/api/kms/my/articles"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/kms/my/articles/history")
    class GetMyRecentArticleHistory {

        @Test
        @DisplayName("Returns 200 OK with recent history")
        void getMyRecentArticleHistory_success() throws Exception {
            MyArticleHistoryDto history = new MyArticleHistoryDto();
            history.setId(1L);
            history.setTitle("최근 수정 문서");
            history.setArticleStatus(ArticleStatus.PENDING);
            history.setUpdatedAt(LocalDateTime.of(2026, 4, 9, 9, 0));

            given(knowledgeArticleMyQueryService.getMyRecentArticleHistory(10L))
                    .willReturn(List.of(history));

            mockMvc.perform(get("/api/kms/my/articles/history").param("authorId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].title").value("최근 수정 문서"))
                    .andExpect(jsonPath("$.data[0].status").value("승인 대기"));
        }

        @Test
        @DisplayName("Returns 400 when authorId is missing")
        void getMyRecentArticleHistory_whenAuthorIdMissing_thenBadRequest() throws Exception {
            mockMvc.perform(get("/api/kms/my/articles/history"))
                    .andExpect(status().isBadRequest());
        }
    }
}
