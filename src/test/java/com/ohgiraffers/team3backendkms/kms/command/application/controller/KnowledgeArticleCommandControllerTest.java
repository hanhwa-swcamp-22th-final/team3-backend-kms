package com.ohgiraffers.team3backendkms.kms.command.application.controller;

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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = KnowledgeArticleCommandController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class KnowledgeArticleCommandControllerTest {

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
        @DisplayName("지식 문서 등록 API 성공: 정상 응답을 반환한다")
        void register_success() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "정상적인 테스트 제목입니다",
                    "category", "TROUBLESHOOTING",
                    "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다."
            );
            willDoNothing().given(knowledgeArticleService)
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("지식 문서 등록 API 실패: 제목이 짧으면 400을 반환한다")
        void register_whenTitleTooShort_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "짧",
                    "category", "TROUBLESHOOTING",
                    "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다."
            );
            willThrow(new IllegalArgumentException("[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."))
                    .given(knowledgeArticleService)
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("지식 문서 등록 API 실패: 본문이 짧으면 400을 반환한다")
        void register_whenContentTooShort_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "정상적인 제목입니다",
                    "category", "TROUBLESHOOTING",
                    "content", "짧은 본문"
            );
            willThrow(new IllegalArgumentException("[ARTICLE_002] 본문은 50자 이상이어야 합니다."))
                    .given(knowledgeArticleService)
                    .register(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            // when & then
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
        @DisplayName("지식 문서 임시저장 API 성공: 정상 응답을 반환한다")
        void draft_success() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "임시 제목",
                    "category", "ETC",
                    "content", "짧아도 됨"
            );
            willDoNothing().given(knowledgeArticleService)
                    .draft(anyLong(), any(), anyString(), any(ArticleCategory.class), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/articles/drafts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/kms/approval/{articleId}/approve")
    class Approve {

        @Test
        @DisplayName("지식 문서 승인 API 성공: 정상 응답을 반환한다")
        void approve_success() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "approverId", 20,
                    "reviewComment", "승인합니다."
            );
            willDoNothing().given(knowledgeArticleService)
                    .approve(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/approval/1/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("지식 문서 승인 API 실패: PENDING이 아니면 400을 반환한다")
        void approve_whenNotPending_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "approverId", 20,
                    "reviewComment", "승인합니다."
            );
            willThrow(new IllegalStateException("[ARTICLE_004] PENDING 상태인 문서만 승인할 수 있습니다."))
                    .given(knowledgeArticleService)
                    .approve(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/approval/1/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/kms/approval/{articleId}/reject")
    class Reject {

        @Test
        @DisplayName("지식 문서 반려 API 성공: 정상 응답을 반환한다")
        void reject_success() throws Exception {
            // given
            Map<String, Object> body = Map.of("reviewComment", "반려 사유입니다. 내용을 보완해주세요.");
            willDoNothing().given(knowledgeArticleService)
                    .reject(anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/approval/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("지식 문서 반려 API 실패: 반려 사유가 없으면 400을 반환한다")
        void reject_whenNoReason_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of("reviewComment", "");
            willThrow(new IllegalArgumentException("[ARTICLE_005] 반려 사유는 10자 이상이어야 합니다."))
                    .given(knowledgeArticleService)
                    .reject(anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/kms/approval/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class Delete {

        @Test
        @DisplayName("지식 문서 삭제 API 성공: 정상 응답을 반환한다")
        void delete_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleService).delete(anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/api/kms/articles/1")
                            .param("requesterId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("지식 문서 삭제 API 실패: 본인 문서가 아니면 400을 반환한다")
        void delete_whenNotAuthor_thenBadRequest() throws Exception {
            // given
            willThrow(new IllegalStateException("[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."))
                    .given(knowledgeArticleService).delete(anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/api/kms/articles/1")
                            .param("requesterId", "99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
