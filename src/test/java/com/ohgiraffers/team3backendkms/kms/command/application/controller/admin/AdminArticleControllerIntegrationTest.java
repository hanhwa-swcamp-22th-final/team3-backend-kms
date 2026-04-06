package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class AdminArticleControllerIntegrationTest {

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
    @DisplayName("Delete article API integration success: save deletion metadata in DB")
    void deleteArticle_success() throws Exception {
        // given
        KnowledgeArticle approvedArticle = saveArticle(ArticleStatus.APPROVED, TITLE, CONTENT);
        String deletionReason = "규정 위반으로 관리자 삭제 처리합니다.";
        Map<String, Object> request = Map.of("deletionReason", deletionReason);

        // when
        mockMvc.perform(delete("/api/kms/admin/articles/{articleId}", approvedArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        flushAndClear();

        // then
        KnowledgeArticle deletedArticle = knowledgeArticleRepository.findById(approvedArticle.getArticleId()).orElseThrow();
        assertTrue(deletedArticle.getIsDeleted());
        assertEquals(deletionReason, deletedArticle.getArticleDeletionReason());
        assertNotNull(deletedArticle.getDeletedAt());
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
