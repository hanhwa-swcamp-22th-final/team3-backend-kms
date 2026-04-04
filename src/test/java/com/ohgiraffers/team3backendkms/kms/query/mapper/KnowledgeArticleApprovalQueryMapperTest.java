package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeArticleApprovalQueryMapperTest {

    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("findApprovalStats()")
    class FindApprovalStats {

        @Test
        @DisplayName("Returns non-null stats with valid numeric values")
        void findApprovalStats_ReturnsStats() {
            // given
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));

            entityManager.flush();
            entityManager.clear();

            // when
            ApprovalStatsDto stats = knowledgeArticleMapper.findApprovalStats();

            // then
            assertNotNull(stats);
            assertTrue(stats.getPendingCount() >= 2);
            assertTrue(stats.getApprovedThisMonth() >= 0);
            assertTrue(stats.getRejectionRate() >= 0.0);
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
