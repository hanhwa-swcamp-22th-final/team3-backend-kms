package com.ohgiraffers.team3backendkms.kms.query.controller;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class KnowledgeArticleQueryControllerIntegrationTest {

    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    private static final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Get article detail API integration success: increase view count after detail lookup")
    void getArticleDetail_success() throws Exception {
        // given
        KnowledgeArticle article = saveArticle(ArticleStatus.APPROVED, TITLE, CONTENT, 3);
        flushAndClear();

        // when
        mockMvc.perform(get("/api/kms/articles/{articleId}", article.getArticleId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.articleId").value(article.getArticleId()))
            .andExpect(jsonPath("$.data.articleTitle").value(TITLE));

        flushAndClear();

        // then
        KnowledgeArticle updatedArticle = knowledgeArticleRepository.findById(article.getArticleId()).orElseThrow();
        assertEquals(4, updatedArticle.getViewCount());
    }

    private KnowledgeArticle saveArticle(ArticleStatus status, String title, String content, int viewCount) {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(AUTHOR_ID)
            .equipmentId(EQUIPMENT_ID)
            .fileGroupId(0L)
            .articleTitle(title)
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent(content)
            .articleStatus(status)
            .approvalVersion(status == ArticleStatus.APPROVED ? 1 : 0)
            .isDeleted(false)
            .viewCount(viewCount)
            .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
