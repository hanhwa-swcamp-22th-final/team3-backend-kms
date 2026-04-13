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

import static com.ohgiraffers.team3backendkms.support.SecurityTestSupport.authenticated;
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
        void getMyArticleStats_success() throws Exception {
            MyArticleStatsDto dto = new MyArticleStatsDto();
            dto.setApprovedCount(3L);
            dto.setPendingCount(1L);
            dto.setRejectedCount(2L);
            dto.setDraftCount(4L);
            given(knowledgeArticleMyQueryService.getMyArticleStats(10L)).willReturn(dto);

            mockMvc.perform(get("/api/kms/my/articles/stats")
                            .with(authenticated(10L, "WORKER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.approvedCount").value(3))
                    .andExpect(jsonPath("$.data.draftCount").value(4));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/my/articles")
    class GetMyArticles {

        @Test
        void getMyArticles_success() throws Exception {
            MyArticleDto article = new MyArticleDto();
            article.setArticleId(1L);
            article.setArticleTitle("내문서");
            article.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            article.setArticleStatus(ArticleStatus.APPROVED);
            article.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));

            KnowledgeTagReadDto tag = new KnowledgeTagReadDto();
            tag.setTagId(100L);
            tag.setTagName("가공");
            article.setTags(List.of(tag));

            given(knowledgeArticleMyQueryService.getMyArticles(any(MyArticleQueryRequest.class))).willReturn(List.of(article));

            mockMvc.perform(get("/api/kms/my/articles")
                            .with(authenticated(10L, "WORKER"))
                            .param("status", "APPROVED")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleId").value(1))
                    .andExpect(jsonPath("$.data[0].tags[0].tagId").value(100));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/my/articles/history")
    class GetMyRecentArticleHistory {

        @Test
        void getMyRecentArticleHistory_success() throws Exception {
            MyArticleHistoryDto history = new MyArticleHistoryDto();
            history.setId(1L);
            history.setTitle("최근 문서");
            history.setArticleStatus(ArticleStatus.PENDING);
            history.setUpdatedAt(LocalDateTime.of(2026, 4, 9, 9, 0));

            given(knowledgeArticleMyQueryService.getMyRecentArticleHistory(10L)).willReturn(List.of(history));

            mockMvc.perform(get("/api/kms/my/articles/history")
                            .with(authenticated(10L, "WORKER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }
    }
}
