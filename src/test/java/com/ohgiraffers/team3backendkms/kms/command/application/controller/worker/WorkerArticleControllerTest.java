package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = WorkerArticleController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class WorkerArticleControllerTest {

    private static final String BASE_URL = "/api/kms/articles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}")
    class Update {

        @Test
        @DisplayName("Update article API success: return successful response")
        void update_success() throws Exception {
            // given
            Map<String, Object> body = createUpdateRequest();
            willDoNothing().given(knowledgeArticleCommandService)
                .update(anyLong(), anyString(), any(ArticleCategory.class), anyString(), anyLong());

            // when & then
            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("requesterId", 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    private Map<String, Object> createRegisterRequest() {
        return Map.of(
            "authorId", 10,
            "equipmentId", 1,
            "title", "정상적인 테스트 제목입니다",
            "category", "TROUBLESHOOTING",
            "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다. 이제 50자를 초과합니다."
        );
    }

    private Map<String, Object> createUpdateRequest() {
        return Map.of(
            "authorId", 10,
            "title", "수정된 제목입니다",
            "category", "PROCESS_IMPROVEMENT",
            "content", "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 이제 충분한 길이입니다."
        );
    }
}
