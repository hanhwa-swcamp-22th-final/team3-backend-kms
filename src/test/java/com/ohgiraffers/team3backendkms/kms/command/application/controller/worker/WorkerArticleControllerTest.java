package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
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
@DisplayName("WorkerArticleController")
class WorkerArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("POST /api/kms/articles")
    class Register {
        @Test
        @DisplayName("Returns 201 Created on valid request")
        void register_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "equipmentId", 1,
                    "title", "정상적인 테스트 제목입니다",
                    "category", "TROUBLESHOOTING",
                    "content", "본문 내용이 50자 이상이어야 합니다. 여기에 충분한 길이의 본문을 작성합니다. 이제 50자를 초과합니다."
            );
            given(knowledgeArticleService.register(anyLong(), anyLong(), anyString(), any(ArticleCategory.class), anyString()))
                    .willReturn(1L);

            mockMvc.perform(post("/api/kms/articles")
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
        @DisplayName("Returns 200 OK on valid request")
        void update_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "수정된 제목입니다",
                    "category", "PROCESS_IMPROVEMENT",
                    "content", "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 이제 충분한 길이입니다."
            );
            willDoNothing().given(knowledgeArticleService)
                    .update(anyLong(), anyString(), any(ArticleCategory.class), anyString(), anyLong());

            mockMvc.perform(put("/api/kms/articles/1")
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
        @DisplayName("Returns 200 OK on valid request")
        void delete_success() throws Exception {
            willDoNothing().given(knowledgeArticleService).delete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/kms/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
