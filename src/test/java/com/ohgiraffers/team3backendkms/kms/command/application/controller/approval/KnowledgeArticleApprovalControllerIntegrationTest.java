package com.ohgiraffers.team3backendkms.kms.command.application.controller.approval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class KnowledgeArticleApprovalControllerIntegrationTest {

    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    private static final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Hold article API integration success: stores review comment without changing status")
    void holdArticle_Success() throws Exception {
        // given
        KnowledgeArticle pendingArticle = saveArticle(ArticleStatus.PENDING, TITLE, CONTENT);
        Map<String, Object> request = Map.of(
            "status", "HOLD",
            "reviewComment", "내용 보완이 필요합니다. 검토 후 재심사 예정입니다."
        );

        // when
        mockMvc.perform(patch("/api/kms/articles/{articleId}/approval", pendingArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        flushAndClear();

        // then
        KnowledgeArticle held = knowledgeArticleRepository.findById(pendingArticle.getArticleId()).orElseThrow();
        assertEquals(ArticleStatus.PENDING, held.getArticleStatus());
        assertEquals("내용 보완이 필요합니다. 검토 후 재심사 예정입니다.", held.getArticleApprovalOpinion());
    }

    @Test
    @DisplayName("Hold article API integration failure: return 400 when status is not PENDING")
    void holdArticle_NotPending_ReturnsBadRequest() throws Exception {
        // given
        KnowledgeArticle draftArticle = saveArticle(ArticleStatus.DRAFT, TITLE, CONTENT);
        Map<String, Object> request = Map.of(
            "status", "HOLD",
            "reviewComment", "DRAFT 상태 문서 보류 시도입니다."
        );

        // when & then
        mockMvc.perform(patch("/api/kms/articles/{articleId}/approval", draftArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    private KnowledgeArticle saveArticle(ArticleStatus status, String title, String content) {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(AUTHOR_ID)
            .equipmentId(EQUIPMENT_ID)
            .fileGroupId(0L)
            .articleTitle(title)
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent(content)
            .articleStatus(status)
            .isDeleted(false)
            .viewCount(0)
            .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
