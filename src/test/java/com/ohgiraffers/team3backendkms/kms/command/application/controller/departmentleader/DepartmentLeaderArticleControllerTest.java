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

    private static final String BASE_URL = "/api/kms/dl/articles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Nested
    @DisplayName("POST /api/kms/dl/articles/{articleId}/approval")
    class ProcessApproval {

        @Test
        @DisplayName("APPROVE: return successful response")
        void approve_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService)
                .processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                    .header("X-Employee-Id", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("status", "APPROVE", "reviewComment", "최종 승인합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("REJECT: return successful response")
        void reject_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService)
                .processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                    .header("X-Employee-Id", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("status", "REJECT", "reviewComment", "반려 사유는 10자 이상이어야 합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("PENDING: return successful response")
        void pending_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService)
                .processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                    .header("X-Employee-Id", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("status", "PENDING", "reviewComment", "내용 보완이 필요합니다. 보류 처리합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("status 없으면 400 반환")
        void missingStatus_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/approval")
                    .header("X-Employee-Id", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("reviewComment", "상태 없는 요청")
                    )))
                .andExpect(status().isBadRequest());
        }
    }
}
