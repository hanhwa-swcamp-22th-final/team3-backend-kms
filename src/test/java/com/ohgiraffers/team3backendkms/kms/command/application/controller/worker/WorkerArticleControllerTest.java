package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleTagCommandService;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = WorkerArticleController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import({GlobalExceptionHandler.class, WorkerArticleControllerTest.SecurityTestConfig.class})
class WorkerArticleControllerTest {

    private static final String BASE_URL = "/api/kms/articles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @MockitoBean
    private KnowledgeArticleTagCommandService knowledgeArticleTagCommandService;

    private EmployeeUserDetails authenticatedWorker() {
        return new EmployeeUserDetails(
                10L,
                "EMP0010",
                List.of(new SimpleGrantedAuthority("WORKER"))
        );
    }

    @TestConfiguration
    static class SecurityTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Nested
    @DisplayName("POST /api/kms/articles")
    class Register {

        @Test
        @DisplayName("Create article API success: return created response")
        void register_success() throws Exception {
            // given
            Map<String, Object> body = createRegisterRequest();
            given(knowledgeArticleCommandService.register(anyLong(), anyLong(), anyString(), any(ArticleCategory.class), anyString()))
                .willReturn(1L);

            // when & then
            mockMvc.perform(post(BASE_URL)
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("문서 등록이 완료되었고 승인 대기 상태로 접수되었습니다."));
        }

        @Test
        @DisplayName("Create article API failure: return 400 when title is blank")
        void register_whenTitleIsBlank_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                "equipmentId", 1,
                "title", "",
                "category", "TROUBLESHOOTING",
                "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다. 이제 50자를 초과합니다."
            );

            // when & then
            mockMvc.perform(post(BASE_URL)
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Create article API failure: return 400 when content is too short")
        void register_whenContentTooShort_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                "equipmentId", 1,
                "title", "정상적인 제목입니다",
                "category", "TROUBLESHOOTING",
                "content", "짧은 본문"
            );

            // when & then
            mockMvc.perform(post(BASE_URL)
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}")
    class Update {

        @Test
        @DisplayName("Update draft API success: return successful response")
        void update_success() throws Exception {
            // given
            Map<String, Object> body = createDraftUpdateRequest();
            willDoNothing().given(knowledgeArticleCommandService)
                .updateDraft(anyLong(), any(), any(), any(), any(), anyLong());

            // when & then
            mockMvc.perform(put(BASE_URL + "/1")
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Update draft API success: allow blank title for temporary save")
        void update_whenTitleIsBlank_thenOk() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                "title", "",
                "category", "TROUBLESHOOTING",
                "content", "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 이제 충분한 길이입니다."
            );
            willDoNothing().given(knowledgeArticleCommandService)
                .updateDraft(anyLong(), any(), any(), any(), any(), anyLong());

            // when & then
            mockMvc.perform(put(BASE_URL + "/1")
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}/revision")
    class StartRevision {

        @Test
        @DisplayName("Start revision API success: return successful response")
        void startRevision_success() throws Exception {
            // given
            given(knowledgeArticleCommandService.startRevision(anyLong(), anyLong())).willReturn(2L);

            // when & then
            mockMvc.perform(put(BASE_URL + "/1/revision")
                    .with(user(authenticatedWorker())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}/submit")
    class Submit {

        @Test
        @DisplayName("Submit draft API success: return successful response")
        void submit_success() throws Exception {
            // given
            Map<String, Object> body = createSubmitRequest();
            willDoNothing().given(knowledgeArticleCommandService)
                .submitDraft(anyLong(), anyString(), any(ArticleCategory.class), anyLong(), anyString(), anyLong());

            // when & then
            mockMvc.perform(put(BASE_URL + "/1/submit")
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Submit draft API failure: return 400 when content is too short")
        void submit_whenContentTooShort_thenBadRequest() throws Exception {
            // given
            Map<String, Object> body = Map.of(
                "equipmentId", 1,
                "title", "정상적인 제목입니다",
                "category", "TROUBLESHOOTING",
                "content", "짧은 본문"
            );

            // when & then
            mockMvc.perform(put(BASE_URL + "/1/submit")
                    .with(user(authenticatedWorker()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class Delete {

        @Test
        @DisplayName("Delete article API success: return successful response")
        void delete_success() throws Exception {
            // given
            willDoNothing().given(knowledgeArticleCommandService).delete(anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete(BASE_URL + "/1")
                    .with(user(authenticatedWorker())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    private Map<String, Object> createRegisterRequest() {
        return Map.of(
            "equipmentId", 1,
            "title", "정상적인 테스트 제목입니다",
            "category", "TROUBLESHOOTING",
            "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다. 이제 50자를 초과합니다."
        );
    }

    private Map<String, Object> createDraftUpdateRequest() {
        return Map.of(
            "equipmentId", 1,
            "title", "수정된 제목입니다",
            "category", "PROCESS_IMPROVEMENT",
            "content", "임시 저장 중인 본문입니다."
        );
    }

    private Map<String, Object> createSubmitRequest() {
        return Map.of(
            "equipmentId", 1,
            "title", "수정된 제목입니다",
            "category", "PROCESS_IMPROVEMENT",
            "content", "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 이제 충분한 길이입니다."
        );
    }
}
