package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

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

import static com.ohgiraffers.team3backendkms.support.SecurityTestSupport.authenticated;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private static final String BASE_URL = "/api/kms/tl/articles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Nested
    @DisplayName("POST /api/kms/tl/articles/{articleId}/approval")
    class ProcessApproval {

        @Test
        void approve_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService).processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                            .with(authenticated(1L, "TEAMLEADER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("status", "APPROVE", "reviewComment", "ok review"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void reject_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService).processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                            .with(authenticated(1L, "TEAMLEADER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("status", "REJECT", "reviewComment", "reject reason long enough"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void pending_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService).processApproval(anyLong(), any(), any(), any());

            mockMvc.perform(post(BASE_URL + "/1/approval")
                            .with(authenticated(1L, "TEAMLEADER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("status", "PENDING", "reviewComment", "hold this for more info"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void missingStatus_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/approval")
                            .with(authenticated(1L, "TEAMLEADER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("reviewComment", "missing status"))))
                    .andExpect(status().isBadRequest());
        }
    }
}
