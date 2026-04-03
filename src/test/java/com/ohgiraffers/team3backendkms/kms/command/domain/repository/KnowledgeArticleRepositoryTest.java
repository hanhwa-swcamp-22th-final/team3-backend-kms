package com.ohgiraffers.team3backendkms.kms.command.domain.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeArticleRepositoryTest {

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private KnowledgeArticle buildArticle(ArticleStatus status) {
        return KnowledgeArticle.builder()
                .articleId(idGenerator.generate())
                .authorId(1L)
                .equipmentId(1L)
                .articleTitle("테스트 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(status)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    @Test
    void saveAndFindById_persistsArticleFields() {
        // given
        KnowledgeArticle article = buildArticle(ArticleStatus.PENDING);

        // when
        Long savedId = knowledgeArticleRepository.save(article).getArticleId();
        Optional<KnowledgeArticle> result = knowledgeArticleRepository.findById(savedId);

        // then
        assertTrue(result.isPresent());
        assertEquals(savedId, result.get().getArticleId());
        assertEquals(ArticleStatus.PENDING, result.get().getArticleStatus());
        assertEquals("테스트 지식 문서 제목입니다", result.get().getArticleTitle());
        assertEquals(ArticleCategory.TROUBLESHOOTING, result.get().getArticleCategory());
        assertFalse(result.get().getIsDeleted());
    }

    @Test
    void deleteById_removesStoredArticle() {
        // given
        KnowledgeArticle savedArticle = knowledgeArticleRepository.save(buildArticle(ArticleStatus.DRAFT));

        // when
        knowledgeArticleRepository.deleteById(savedArticle.getArticleId());

        // then
        assertTrue(knowledgeArticleRepository.findById(savedArticle.getArticleId()).isEmpty());
    }
}
