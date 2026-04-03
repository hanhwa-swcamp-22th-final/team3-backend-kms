package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

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
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminArticleController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class AdminArticleControllerTest {

    private static final String BASE_URL = "/api/kms/admin/articles";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Nested
    @DisplayName("DELETE /api/kms/admin/articles/{articleId}")
    class AdminDelete {

        @Test
        @DisplayName("Delete article API success: return successful response")
        void adminDelete_success() throws Exception {
            // given
            Map<String, String> body = Map.of("deletionReason", "규정 위반 문서 삭제 (10자 이상)");
            willDoNothing().given(knowledgeArticleCommandService).adminDelete(anyLong(), anyString());

            // when & then
            mockMvc.perform(delete(BASE_URL + "/1")
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
        @DisplayName("Update article API success: return successful response")
        void adminUpdate_success() throws Exception {
            // given
            Map<String, Object> body = createAdminUpdateRequest();
            willDoNothing().given(knowledgeArticleCommandService).adminUpdate(anyLong(), anyString(), any(ArticleCategory.class), anyString());

            // when & then
            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    private Map<String, Object> createAdminUpdateRequest() {
        return Map.of(
            "title", "관리자 수정 제목입니다 (5자 이상)",
            "category", "ETC",
            "content", "관리자가 수정한 본문 내용입니다. 이 본문은 최소 50자 이상이어야 검증을 통과할 수 있습니다. 룰루랄라 룰루랄라 충분한 길이 확보."
        );
    }
}
