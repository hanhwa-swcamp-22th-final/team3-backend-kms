package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleApprovalService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = TeamLeaderArticleController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class TeamLeaderArticleControllerTest {

    private static final String BASE_URL = "/api/kms/tl/approval";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleApprovalService knowledgeArticleApprovalService;

    @Nested
    @DisplayName("POST /api/kms/tl/approval/{articleId}/approve")
    class Approve {

        @Test
        @DisplayName("Approve article API success: return successful response")
        void approve_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleApprovalService)
                .approve(anyLong(), anyLong(), anyString());

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
    @DisplayName("POST /api/kms/tl/approval/{articleId}/reject")
    class Reject {

        @Test
        @DisplayName("Reject article API success: return successful response")
        void reject_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleApprovalService)
                .reject(anyLong(), anyString());

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("reviewComment", "반려 사유는 10자 이상이어야 합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }
}
