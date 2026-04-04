package com.ohgiraffers.team3backendkms.kms.command.application.controller.approval;

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
    controllers = KnowledgeArticleApprovalController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class KnowledgeArticleApprovalControllerTest {

    private static final String BASE_URL = "/api/kms/approval";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleApprovalService knowledgeArticleApprovalService;

    @Nested
    @DisplayName("POST /api/kms/approval/{articleId}/hold")
    class Hold {

        @Test
        @DisplayName("Hold article API success: return 200 OK")
        void hold_Success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleApprovalService)
                .hold(anyLong(), anyString());

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/hold")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("reviewComment", "내용 보완이 필요합니다. 보류 처리합니다.")
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Hold article API failure: return 400 when reviewComment is blank")
        void hold_WhenReviewCommentBlank_ThenBadRequest() throws Exception {
            // given
            Map<String, String> body = Map.of("reviewComment", "");

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/hold")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Hold article API failure: return 400 when reviewComment exceeds 500 chars")
        void hold_WhenReviewCommentTooLong_ThenBadRequest() throws Exception {
            // given
            String longComment = "가".repeat(501);
            Map<String, String> body = Map.of("reviewComment", longComment);

            // when & then
            mockMvc.perform(post(BASE_URL + "/1/hold")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }
}
