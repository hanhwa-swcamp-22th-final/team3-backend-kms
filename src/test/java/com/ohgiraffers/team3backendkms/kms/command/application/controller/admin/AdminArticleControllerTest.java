package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

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
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminArticleController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminArticleController")
class AdminArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleService knowledgeArticleService;

    @Nested
    @DisplayName("DELETE /api/kms/admin/articles/{articleId}")
    class AdminDelete {
        @Test
        @DisplayName("Returns 200 OK on valid request")
        void adminDelete_success() throws Exception {
            Map<String, String> body = Map.of("deletionReason", "규정 위반 문서 삭제 (10자 이상)");
            willDoNothing().given(knowledgeArticleService).adminDelete(anyLong(), anyString());

            mockMvc.perform(delete("/api/kms/admin/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/kms/admin/articles/{articleId}")
    class AdminUpdate {
        @Test
        @DisplayName("Returns 200 OK on valid request")
        void adminUpdate_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "authorId", 10,
                    "title", "관리자 수정 제목입니다 (5자 이상)",
                    "category", "ETC",
                    "content", "관리자가 수정한 본문 내용입니다. 이 본문은 최소 50자 이상이어야 검증을 통과할 수 있습니다. 룰루랄라 룰루랄라 충분한 길이 확보."
            );
            willDoNothing().given(knowledgeArticleService).adminUpdate(anyLong(), anyString(), any(ArticleCategory.class), anyString());

            mockMvc.perform(put("/api/kms/admin/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
