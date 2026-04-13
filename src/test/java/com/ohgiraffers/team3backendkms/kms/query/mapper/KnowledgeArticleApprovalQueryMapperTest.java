package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeArticleApprovalQueryMapperTest {

    private Long authorId;
    private Long equipmentId;

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        authorId = jdbcTemplate.queryForObject(
            "SELECT employee_id FROM employee " +
                "WHERE employee_name NOT IN ('Batch-Test-Worker', '최상위관리자') " +
                "LIMIT 1",
            Long.class
        );
        equipmentId = jdbcTemplate.queryForObject(
            "SELECT equipment_id FROM equipment LIMIT 1",
            Long.class
        );
    }

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

    @Nested
    @DisplayName("findApprovalArticles()")
    class FindApprovalArticles {

        @Test
        @DisplayName("Returns PENDING articles when no filter applied")
        void findApprovalArticles_NoFilter_ReturnsPendingList() {
            // given
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            knowledgeArticleRepository.save(buildArticle(ArticleStatus.DRAFT));

            entityManager.flush();
            entityManager.clear();

            // when
            List<ApprovalArticleDto> result = knowledgeArticleMapper.findApprovalArticles(new ApprovalQueryRequest());

            // then
            assertFalse(result.isEmpty());
            result.forEach(dto -> assertTrue(
                dto.getArticleStatus().name().equals("PENDING"),
                "목록에 PENDING 아닌 항목이 포함됨: " + dto.getArticleStatus()
            ));
        }
    }

    @Nested
    @DisplayName("findApprovalArticleById()")
    class FindApprovalArticleById {

        @Test
        @DisplayName("Returns detail when PENDING article exists")
        void findApprovalArticleById_ReturnDetail() {
            // given
            KnowledgeArticle article = knowledgeArticleRepository.save(buildArticle(ArticleStatus.PENDING));
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<ApprovalArticleDetailDto> result = knowledgeArticleMapper.findApprovalArticleById(article.getArticleId());

            // then
            assertTrue(result.isPresent());
            assertNotNull(result.get().getArticleTitle());
        }

        @Test
        @DisplayName("Returns empty when article not found")
        void findApprovalArticleById_NotFound_ReturnsEmpty() {
            // when
            Optional<ApprovalArticleDetailDto> result = knowledgeArticleMapper.findApprovalArticleById(-1L);

            // then
            assertTrue(result.isEmpty());
        }
    }

    private KnowledgeArticle buildArticle(ArticleStatus status) {
        return KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(authorId)
            .equipmentId(equipmentId)
            .fileGroupId(0L)
            .articleTitle("매퍼 테스트용 제목입니다")
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent("매퍼 테스트용 본문입니다. 최소 50자 이상이어야 합니다. 충분한 길이를 확보했습니다.")
            .articleStatus(status)
            .approvalVersion(status == ArticleStatus.APPROVED ? 1 : 0)
            .isDeleted(false)
            .viewCount(0)
            .build();
    }
}
