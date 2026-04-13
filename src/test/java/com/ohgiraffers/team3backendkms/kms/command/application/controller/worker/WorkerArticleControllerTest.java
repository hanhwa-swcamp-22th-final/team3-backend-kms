package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.ohgiraffers.team3backendkms.support.SecurityTestSupport.authenticated;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockitoBean
    private KnowledgeArticleTagCommandService knowledgeArticleTagCommandService;

    @Nested
    class Register {
        @Test
        void register_success() throws Exception {
            given(knowledgeArticleCommandService.register(anyLong(), anyLong(), anyString(), any(ArticleCategory.class), anyString()))
                    .willReturn(1L);

            mockMvc.perform(post(BASE_URL)
                            .with(authenticated(10L, "WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRegisterRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    class Update {
        @Test
        void update_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService)
                    .updateDraft(anyLong(), any(), any(), any(), any(), anyLong());

            mockMvc.perform(put(BASE_URL + "/1")
                            .with(authenticated(10L, "WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDraftUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    class StartRevision {
        @Test
        void startRevision_success() throws Exception {
            given(knowledgeArticleCommandService.startRevision(anyLong(), anyLong())).willReturn(2L);

            mockMvc.perform(put(BASE_URL + "/1/revision")
                            .with(authenticated(10L, "WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    class Submit {
        @Test
        void submit_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService)
                    .submitDraft(anyLong(), anyString(), any(ArticleCategory.class), anyLong(), anyString(), anyLong());

            mockMvc.perform(put(BASE_URL + "/1/submit")
                            .with(authenticated(10L, "WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSubmitRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_success() throws Exception {
            willDoNothing().given(knowledgeArticleCommandService).delete(anyLong(), anyLong());

            mockMvc.perform(delete(BASE_URL + "/1")
                            .with(authenticated(10L, "WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    private Map<String, Object> createRegisterRequest() {
        return Map.of(
                "equipmentId", 1,
                "title", "정상 제목입니다",
                "category", "TROUBLESHOOTING",
                "content", "본문 내용은 오십자 이상이어야 하므로 충분한 길이로 작성한 테스트용 본문입니다. 길이를 더 확보합니다."
        );
    }

    private Map<String, Object> createDraftUpdateRequest() {
        return Map.of(
                "equipmentId", 1,
                "title", "수정 제목입니다",
                "category", "PROCESS_IMPROVEMENT",
                "content", "임시 저장용 본문입니다."
        );
    }

    private Map<String, Object> createSubmitRequest() {
        return Map.of(
                "equipmentId", 1,
                "title", "제출 제목입니다",
                "category", "PROCESS_IMPROVEMENT",
                "content", "제출 본문은 오십자 이상이어야 하므로 충분한 길이로 작성한 테스트용 본문입니다. 길이를 더 확보합니다."
        );
    }
}
