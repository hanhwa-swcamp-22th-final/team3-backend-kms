package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeArticleQueryMapperTest {

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
    private Long validEquipmentId;

    private static final Long TEST_ARTICLE_ID_1 = 9000000000001L;
    private static final Long TEST_ARTICLE_ID_2 = 9000000000002L;
    private static final Long TEST_ARTICLE_ID_3 = 9000000000003L;
    private static final Long TEST_ARTICLE_ID_4 = 9000000000004L;
    private static final Long TEST_EQUIPMENT_ID = 9000000099L;

    @BeforeEach
    void setUp() {
        // given
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO equipment " +
                        "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
                        "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-MAPPER', '매퍼테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        validAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee LIMIT 1", Long.class
        );
        otherAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee WHERE employee_id <> " + validAuthorId + " LIMIT 1", Long.class
        );
        validEquipmentId = TEST_EQUIPMENT_ID;

        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_1,
                validAuthorId,
                "테스트 문서 제목 첫번째",
                ArticleCategory.TROUBLESHOOTING,
                "첫번째 문서 본문 내용입니다.",
                ArticleStatus.APPROVED,
                false,
                10,
                LocalDateTime.now().minusDays(2)
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_2,
                validAuthorId,
                "테스트 문서 제목 두번째",
                ArticleCategory.SAFETY,
                "두번째 문서 본문 내용입니다.",
                ArticleStatus.PENDING,
                false,
                5,
                LocalDateTime.now().minusDays(1)
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_3,
                validAuthorId,
                "삭제된 문서 제목",
                ArticleCategory.TROUBLESHOOTING,
                "삭제된 문서 본문 내용입니다.",
                ArticleStatus.DRAFT,
                true,
                0,
                LocalDateTime.now()
        ));
        knowledgeArticleRepository.save(buildArticle(
                TEST_ARTICLE_ID_4,
                otherAuthorId,
                "다른 작성자의 임시 문서",
                ArticleCategory.PROCESS_IMPROVEMENT,
                "다른 작성자의 비승인 문서 본문 내용입니다.",
                ArticleStatus.DRAFT,
                false,
                1,
                LocalDateTime.now().minusHours(3)
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findArticles()")
    class FindArticles {

        @Test
        @DisplayName("Returns list excluding deleted articles")
        void findArticles_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_3)));
        }

        @Test
        @DisplayName("Filters by category")
        void findArticles_withCategoryFilter_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setCategory(ArticleCategory.TROUBLESHOOTING);

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.stream().allMatch(article ->
                    article.getArticleCategory() == ArticleCategory.TROUBLESHOOTING
            ));
        }

        @Test
        @DisplayName("Sorts by view count when sort=popular")
        void findArticles_withSort_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSort("popular");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getViewCount() >= result.get(i + 1).getViewCount());
            }
        }

        @Test
        @DisplayName("Filters by article title keyword")
        void findArticles_withArticleTitleKeyword_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleTitle");
            request.setKeyword("첫번째");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Filters by author name keyword")
        void findArticles_withAuthorNameKeyword_success() {
            // given
            String otherAuthorName = jdbcTemplate.queryForObject(
                    "SELECT employee_name FROM employee WHERE employee_id = " + otherAuthorId,
                    String.class
            );
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("authorName");
            request.setKeyword(otherAuthorName);

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(otherAuthorId, result.get(0).getAuthorId());
        }

        @Test
        @DisplayName("Filters by article ID keyword")
        void findArticles_withArticleIdKeyword_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleId");
            request.setKeyword(String.valueOf(TEST_ARTICLE_ID_1));
            request.setArticleIdKeyword(TEST_ARTICLE_ID_1);

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Worker can see own articles and approved articles only")
        void findArticles_withWorkerVisibility_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterId(validAuthorId);
            request.setRequesterRole("WORKER");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_4)));
        }

        @Test
        @DisplayName("Team leader cannot see draft articles")
        void findArticles_withTeamLeaderVisibility_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterRole("TEAMLEADER");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_4)));
        }

        @Test
        @DisplayName("Admin can see all articles including deleted articles")
        void findArticles_withAdminVisibility_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterRole("ADMIN");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_3)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_4)));
        }
    }

    @Nested
    @DisplayName("findArticleById()")
    class FindArticleById {

        @Test
        @DisplayName("Returns article detail by ID")
        void findArticleById_success() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(TEST_ARTICLE_ID_1);

            // then
            assertTrue(result.isPresent());
            assertEquals(TEST_ARTICLE_ID_1, result.get().getArticleId());
            assertEquals("테스트 문서 제목 첫번째", result.get().getArticleTitle());
            assertEquals(ArticleCategory.TROUBLESHOOTING, result.get().getArticleCategory());
        }

        @Test
        @DisplayName("Returns empty when ID does not exist")
        void findArticleById_whenUnknownId_thenEmpty() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(9999999999999L);

            // then
            assertTrue(result.isEmpty());
        }
    }

    private KnowledgeArticle buildArticle(Long articleId,
                                          Long authorId,
                                          String title,
                                          ArticleCategory category,
                                          String content,
                                          ArticleStatus status,
                                          boolean isDeleted,
                                          int viewCount,
                                          LocalDateTime createdAt) {
        return KnowledgeArticle.builder()
                .articleId(articleId)
                .authorId(authorId)
                .equipmentId(validEquipmentId)
                .fileGroupId(0L)
                .articleTitle(title)
                .articleCategory(category)
                .articleContent(content)
                .articleStatus(status)
                .approvalVersion(0)
                .isDeleted(isDeleted)
                .viewCount(viewCount)
                .createdAt(createdAt)
                .build();
    }
}
