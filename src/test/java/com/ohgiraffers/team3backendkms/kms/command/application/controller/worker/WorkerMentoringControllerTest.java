package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.MentoringCommandService;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WorkerMentoringController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class WorkerMentoringControllerTest {

    private static final String BASE_URL = "/api/kms/mentoring/requests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MentoringCommandService mentoringCommandService;

    @Nested
    @DisplayName("POST /api/kms/mentoring/requests")
    class CreateRequest {

        @Test
        @DisplayName("Create mentoring request API success: return created response")
        void createRequest_success() throws Exception {
            given(mentoringCommandService.createRequest(
                    anyLong(), any(), any(), any(), any(), any(), any(), any(RequestPriority.class)))
                    .willReturn(100L);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequestBody())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("멘토링 요청이 등록되었고 멘토 수락 대기 상태가 되었습니다."))
                    .andExpect(jsonPath("$.data").value(100));
        }

        @Test
        @DisplayName("Create mentoring request API failure: return 400 when mentoring field is blank")
        void createRequest_whenMentoringFieldBlank_thenBadRequest() throws Exception {
            Map<String, Object> body = createRequestBody();
            body.put("mentoringField", "");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    private Map<String, Object> createRequestBody() {
        return new java.util.HashMap<>(Map.of(
                "menteeId", 10,
                "articleId", 101,
                "mentoringField", "설비보전",
                "requestTitle", "설비보전 멘토링 요청",
                "requestContent", "설비 점검 기준과 트러블슈팅 절차에 대해 멘토링을 받고 싶습니다.",
                "mentoringDurationWeeks", 4,
                "mentoringFrequency", "주 2회",
                "requestPriority", "HIGH"
        ));
    }
}
