package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminArticleController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminArticleController")
class AdminArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("DELETE /api/kms/admin/articles/{articleId}")
    class AdminDelete {

        @Test
        @DisplayName("Returns 200 OK on valid request")
        void adminDelete_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "deletionReason", "지식 문서 정책 위반으로 인한 삭제입니다. 해당 문서는 더 이상 참고할 수 없습니다."
            );
            willDoNothing().given(knowledgeArticleService).adminDelete(anyLong(), anyString());

            mockMvc.perform(delete("/api/kms/admin/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Returns 400 when deletion reason is too short")
        void adminDelete_whenReasonTooShort_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of(
                    "deletionReason", "짧음"
            );
            willThrow(new IllegalArgumentException("[ARTICLE_012] 삭제 사유는 10자 이상 500자 이하여야 합니다."))
                    .given(knowledgeArticleService).adminDelete(anyLong(), anyString());

            mockMvc.perform(delete("/api/kms/admin/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Returns 400 when deletion reason is too long")
        void adminDelete_whenReasonTooLong_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of(
                    "deletionReason", "a".repeat(501)
            );
            willThrow(new IllegalArgumentException("[ARTICLE_012] 삭제 사유는 10자 이상 500자 이하여야 합니다."))
                    .given(knowledgeArticleService).adminDelete(anyLong(), anyString());

            mockMvc.perform(delete("/api/kms/admin/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
