package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.PendingArticleQueryRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PendingArticleQueryMapperTest {

    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("findPendingStats()")
    class FindPendingStats {

        @Test
        @DisplayName("Returns non-null stats with valid numeric values")
        void findPendingStats_ReturnsStats() {
            // given
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));

            entityManager.flush();
            entityManager.clear();

            // when
            PendingArticleStatsDto stats = knowledgeArticleMapper.findPendingStats();

            // then
            assertNotNull(stats);
            assertTrue(stats.getPendingCount() >= 2);
            assertTrue(stats.getApprovedThisMonth() >= 0);
            assertTrue(stats.getRejectionRate() >= 0.0);
        }
    }

    @Nested
    @DisplayName("findPendingArticles()")
    class FindPendingArticles {

        @Test
        @DisplayName("Returns PENDING articles when no filter applied")
        void findPendingArticles_NoFilter_ReturnsPendingList() {
            // given
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.DRAFT));

            entityManager.flush();
            entityManager.clear();

            // when
            List<PendingArticleDto> result = knowledgeArticleMapper.findPendingArticles(new PendingArticleQueryRequest());

            // then
            assertFalse(result.isEmpty());
            result.forEach(dto -> assertTrue(
                dto.getArticleStatus().name().equals("PENDING"),
                "목록에 PENDING 아닌 항목이 포함됨: " + dto.getArticleStatus()
            ));
        }
    }

    @Nested
    @DisplayName("findPendingArticleById()")
    class FindPendingArticleById {

        @Test
        @DisplayName("Returns detail when PENDING article exists")
        void findPendingArticleById_ReturnDetail() {
            // given
            KnowledgeArticle article = knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<PendingArticleDetailDto> result = knowledgeArticleMapper.findPendingArticleById(article.getArticleId());

            // then
            assertTrue(result.isPresent());
            assertNotNull(result.get().getArticleTitle());
        }

        @Test
        @DisplayName("Returns empty when article not found")
        void findPendingArticleById_NotFound_ReturnsEmpty() {
            // when
            Optional<PendingArticleDetailDto> result = knowledgeArticleMapper.findPendingArticleById(-1L);

            // then
            assertTrue(result.isEmpty());
        }
    }

    private KnowledgeArticle buildArticle(ArticleStatus status) {
        return KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(AUTHOR_ID)
            .equipmentId(EQUIPMENT_ID)
            .fileGroupId(0L)
            .articleTitle("매퍼 테스트용 제목입니다")
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent("매퍼 테스트용 본문입니다. 최소 50자 이상이어야 합니다. 충분한 길이를 확보했습니다.")
            .articleStatus(status)
            .isDeleted(false)
            .viewCount(0)
            .build();
    }
}
