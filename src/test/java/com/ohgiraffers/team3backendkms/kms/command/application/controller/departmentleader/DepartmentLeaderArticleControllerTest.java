package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DepartmentLeaderArticleController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class DepartmentLeaderArticleControllerTest {

    private static final String BASE_URL = "/api/kms/dl/approval";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Nested
    @DisplayName("POST /api/kms/dl/approval/{articleId}/approve")
    class Approve {

        @Test
        @DisplayName("Approve article API success: return successful response")
        void approve_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleCommandService)
                .processApproval(anyLong(), any(), any(), anyString());

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("approverId", 10, "reviewComment", "최종 승인합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/kms/dl/approval/{articleId}/reject")
    class Reject {

        @Test
        @DisplayName("Reject article API success: return successful response")
        void reject_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleCommandService)
                .processApproval(anyLong(), any(), any(), anyString());

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of(
                                "approverId", 10,
                                "reviewComment", "반려 사유는 10자 이상이어야 합니다."
                        )
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Reject article API failure: return 400 when reviewComment is too short")
        void reject_whenReviewCommentTooShort_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                    "approverId", 10,
                    "reviewComment", "짧음"
            );

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }
}
