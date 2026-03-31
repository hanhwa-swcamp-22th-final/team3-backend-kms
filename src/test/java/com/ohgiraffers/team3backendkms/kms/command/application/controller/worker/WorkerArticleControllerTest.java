package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = WorkerArticleController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
@DisplayName("WorkerArticleController")
class WorkerArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("POST /api/kms/articles")
    class Register {

        @Test
        @DisplayName("Returns 201 Created on valid request")
        void register_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "정상적인 테스트 제목입니다",
                    "category", "TROUBLESHOOTING",
                    "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다."
            );
            given(knowledgeArticleService
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString()))
                    .willReturn(1L);

            mockMvc.perform(post("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(1));
        }

        @Test
        @DisplayName("Returns 400 when title is too short")
        void register_whenTitleTooShort_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "짧",
                    "category", "TROUBLESHOOTING",
                    "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다."
            );
            willThrow(new IllegalArgumentException("[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."))
                    .given(knowledgeArticleService)
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            mockMvc.perform(post("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("Returns 400 when content is too short")
        void register_whenContentTooShort_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "정상적인 제목입니다",
                    "category", "TROUBLESHOOTING",
                    "content", "짧은 본문"
            );
            willThrow(new IllegalArgumentException("[ARTICLE_002] 본문은 50자 이상이어야 합니다."))
                    .given(knowledgeArticleService)
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            mockMvc.perform(post("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/kms/articles/drafts")
    class Draft {

        @Test
        @DisplayName("Returns 201 Created on valid request")
        void draft_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "임시 제목",
                    "category", "ETC",
                    "content", "짧아도 됨"
            );
            given(knowledgeArticleService
                    .draft(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString()))
                    .willReturn(2L);

            mockMvc.perform(post("/api/kms/articles/drafts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class Delete {

        @Test
        @DisplayName("Returns 200 OK on valid request")
        void delete_success() throws Exception {
            willDoNothing().given(knowledgeArticleService).delete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/kms/articles/1")
                            .param("requesterId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Returns 400 when requester is not the author")
        void delete_whenNotAuthor_thenBadRequest() throws Exception {
            willThrow(new IllegalStateException("[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."))
                    .given(knowledgeArticleService).delete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/kms/articles/1")
                            .param("requesterId", "99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
