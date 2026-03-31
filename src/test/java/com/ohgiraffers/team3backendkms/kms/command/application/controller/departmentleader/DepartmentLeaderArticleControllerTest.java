package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DepartmentLeaderArticleController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
@DisplayName("DepartmentLeaderArticleController")
class DepartmentLeaderArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("POST /api/kms/dl/approval/{articleId}/approve")
    class Approve {

        @Test
        @DisplayName("Returns 200 OK on valid request")
        void approve_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "approverId", 20,
                    "reviewComment", "최종 승인합니다."
            );
            willDoNothing().given(knowledgeArticleService)
                    .approve(anyLong(), anyLong(), anyString());

            mockMvc.perform(post("/api/kms/dl/approval/1/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Returns 400 when status is not PENDING")
        void approve_whenNotPending_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of(
                    "approverId", 20,
                    "reviewComment", "최종 승인합니다."
            );
            willThrow(new IllegalStateException("[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."))
                    .given(knowledgeArticleService)
                    .approve(anyLong(), anyLong(), anyString());

            mockMvc.perform(post("/api/kms/dl/approval/1/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/kms/dl/approval/{articleId}/reject")
    class Reject {

        @Test
        @DisplayName("Returns 200 OK on valid request")
        void reject_success() throws Exception {
            Map<String, Object> body = Map.of("reviewComment", "반려 사유입니다. 내용을 보완해주세요.");
            willDoNothing().given(knowledgeArticleService)
                    .reject(anyLong(), anyString());

            mockMvc.perform(post("/api/kms/dl/approval/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Returns 400 when rejection reason is empty")
        void reject_whenNoReason_thenBadRequest() throws Exception {
            Map<String, Object> body = Map.of("reviewComment", "");
            willThrow(new IllegalArgumentException("[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."))
                    .given(knowledgeArticleService)
                    .reject(anyLong(), anyString());

            mockMvc.perform(post("/api/kms/dl/approval/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
