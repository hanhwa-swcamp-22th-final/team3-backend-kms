package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeArticleMyQueryMapperTest {

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long validAuthorId;
    private Long otherAuthorId;

    // 기존 MapperTest와 충돌 방지를 위해 별도 ID 범위 사용
    private static final Long TEST_ARTICLE_ID_APPROVED  = 9000000001001L;
    private static final Long TEST_ARTICLE_ID_PENDING   = 9000000001002L;
    private static final Long TEST_ARTICLE_ID_REJECTED  = 9000000001003L;
    private static final Long TEST_ARTICLE_ID_DRAFT     = 9000000001004L;
    private static final Long TEST_ARTICLE_ID_DELETED   = 9000000001005L;
    private static final Long TEST_ARTICLE_ID_OTHER     = 9000000001006L;
    private static final Long TEST_EQUIPMENT_ID         = 9000000098L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO equipment " +
                        "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
                        "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-MY-MAPPER', '마이매퍼테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        validAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee LIMIT 1", Long.class
        );
        otherAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee WHERE employee_id <> " + validAuthorId + " LIMIT 1", Long.class
        );

        // validAuthorId 문서: APPROVED, PENDING, REJECTED, DRAFT (is_deleted=false)
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_APPROVED, validAuthorId, "승인된 문서 제목입니다",
                ArticleCategory.TROUBLESHOOTING, ArticleStatus.APPROVED, false, LocalDateTime.now().minusDays(3)
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_PENDING, validAuthorId, "대기중인 문서 제목입니다",
                ArticleCategory.SAFETY, ArticleStatus.PENDING, false, LocalDateTime.now().minusDays(2)
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_REJECTED, validAuthorId, "반려된 문서 제목입니다",
                ArticleCategory.PROCESS_IMPROVEMENT, ArticleStatus.REJECTED, false, LocalDateTime.now().minusDays(1)
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_DRAFT, validAuthorId, "임시저장 문서 제목입니다",
                ArticleCategory.EQUIPMENT_OPERATION, ArticleStatus.DRAFT, false, LocalDateTime.now()
        ));

        // validAuthorId 문서: DRAFT (is_deleted=true) — 통계/목록에서 제외되어야 함
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_DELETED, validAuthorId, "삭제된 문서 제목입니다",
                ArticleCategory.TROUBLESHOOTING, ArticleStatus.DRAFT, true, LocalDateTime.now()
        ));

        // otherAuthorId 문서 — 내 글 조회에서 제외되어야 함
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_OTHER, otherAuthorId, "다른 작성자의 문서입니다",
                ArticleCategory.SAFETY, ArticleStatus.APPROVED, false, LocalDateTime.now()
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findMyArticleStats()")
    class FindMyArticleStats {

        @Test
        @DisplayName("Returns counts for each status")
        void findMyArticleStats_success() {
            // when
            MyArticleStatsDto result = knowledgeArticleMapper.findMyArticleStats(validAuthorId);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getApprovedCount());
            assertEquals(1L, result.getPendingCount());
            assertEquals(1L, result.getRejectedCount());
            assertEquals(1L, result.getDraftCount());
        }

        @Test
        @DisplayName("Excludes deleted articles from stats")
        void findMyArticleStats_excludesDeleted() {
            // when
            MyArticleStatsDto result = knowledgeArticleMapper.findMyArticleStats(validAuthorId);

            // then
            // draftCount는 1 (삭제된 DRAFT는 제외)
            assertEquals(1L, result.getDraftCount());
        }

        @Test
        @DisplayName("Returns zero counts for unknown authorId")
        void findMyArticleStats_whenNoArticles_thenZero() {
            // when
            MyArticleStatsDto result = knowledgeArticleMapper.findMyArticleStats(-1L);

            // then
            assertNotNull(result);
            assertEquals(0L, result.getApprovedCount());
            assertEquals(0L, result.getPendingCount());
            assertEquals(0L, result.getRejectedCount());
            assertEquals(0L, result.getDraftCount());
        }
    }

    @Nested
    @DisplayName("findMyArticles()")
    class FindMyArticles {

        @Test
        @DisplayName("Returns only own articles excluding deleted")
        void findMyArticles_success() {
            // given
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            request.setPage(0);
            request.setSize(10);

            // when
            List<MyArticleDto> result = knowledgeArticleMapper.findMyArticles(validAuthorId, request);

            // then
            assertNotNull(result);
            assertEquals(4, result.size());
            assertTrue(result.stream().noneMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_DELETED)));
            assertTrue(result.stream().noneMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_OTHER)));
        }

        @Test
        @DisplayName("Filters by status")
        void findMyArticles_withStatusFilter_success() {
            // given
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            request.setStatus(ArticleStatus.APPROVED);
            request.setPage(0);
            request.setSize(10);

            // when
            List<MyArticleDto> result = knowledgeArticleMapper.findMyArticles(validAuthorId, request);

            // then
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_APPROVED, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Sorts by createdAt DESC by default")
        void findMyArticles_sortedByCreatedAtDesc() {
            // given
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            request.setPage(0);
            request.setSize(10);

            // when
            List<MyArticleDto> result = knowledgeArticleMapper.findMyArticles(validAuthorId, request);

            // then
            for (int i = 0; i < result.size() - 1; i++) {
                assertFalse(result.get(i).getCreatedAt().isBefore(result.get(i + 1).getCreatedAt()));
            }
        }

        @Test
        @DisplayName("Applies paging correctly")
        void findMyArticles_withPaging_success() {
            // given
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            request.setPage(0);
            request.setSize(2);

            // when
            List<MyArticleDto> result = knowledgeArticleMapper.findMyArticles(validAuthorId, request);

            // then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Returns empty list when no articles exist")
        void findMyArticles_whenNoArticles_thenEmpty() {
            // given
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            request.setPage(0);
            request.setSize(10);

            // when
            List<MyArticleDto> result = knowledgeArticleMapper.findMyArticles(-1L, request);

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findMyRecentArticleHistory()")
    class FindMyRecentArticleHistory {

        @Test
        @DisplayName("Returns latest three articles by updatedAt")
        void findMyRecentArticleHistory_success() {
            List<MyArticleHistoryDto> result = knowledgeArticleMapper.findMyRecentArticleHistory(validAuthorId);

            assertNotNull(result);
            assertEquals(3, result.size());
            for (int i = 0; i < result.size() - 1; i++) {
                assertFalse(result.get(i).getUpdatedAt().isBefore(result.get(i + 1).getUpdatedAt()));
            }
        }

        @Test
        @DisplayName("Excludes deleted and other author's articles")
        void findMyRecentArticleHistory_filtersCorrectly() {
            List<MyArticleHistoryDto> result = knowledgeArticleMapper.findMyRecentArticleHistory(validAuthorId);

            assertTrue(result.stream().noneMatch(h -> h.getId().equals(TEST_ARTICLE_ID_DELETED)));
            assertTrue(result.stream().noneMatch(h -> h.getId().equals(TEST_ARTICLE_ID_OTHER)));
        }
    }

    private KnowledgeArticle buildArticle(Long articleId, Long authorId, String title,
                                          ArticleCategory category, ArticleStatus status,
                                          boolean isDeleted, LocalDateTime createdAt) {
        return KnowledgeArticle.builder()
                .articleId(articleId)
                .authorId(authorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(title)
                .articleCategory(category)
                .articleContent("본문 내용이 들어갑니다. 테스트용 본문입니다.")
                .articleStatus(status)
                .approvalVersion(0)
                .isDeleted(isDeleted)
                .viewCount(0)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
