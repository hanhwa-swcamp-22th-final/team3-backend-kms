package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.EquipmentDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.TagDto;
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
    private static final Long TEST_TAG_ID_1 = 9000000001L;
    private static final Long TEST_TAG_ID_2 = 9000000002L;

    @BeforeEach
    void setUp() {
        // given
        // 테스트에 필요한 보조 테이블과 기준 데이터를 먼저 준비한다.
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO equipment " +
                        "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
                        "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-MAPPER', '매퍼테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO knowledge_tag (tag_id, tag_name) VALUES (" + TEST_TAG_ID_1 + ", 'TEST-태그-알파')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO knowledge_tag (tag_id, tag_name) VALUES (" + TEST_TAG_ID_2 + ", 'TEST-태그-베타')"
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

        // when
        // JPA로 저장한 데이터를 실제 DB에 반영하고 영속성 컨텍스트를 비운다.
        entityManager.flush();
        entityManager.clear();

        // then
        // 각 테스트가 동일한 초기 데이터 상태에서 시작할 수 있게 된다.
    }

    @Nested
    @DisplayName("findArticles()")
    class FindArticles {

        @Test
        @DisplayName("Returns list excluding deleted articles")
        void findArticles_success() {
            // given
            // 필터 없는 기본 목록 조회 요청을 만든다.
            ArticleQueryRequest request = new ArticleQueryRequest();

            // when
            // 매퍼를 호출해 지식 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 삭제된 문서는 목록 결과에 포함되지 않아야 한다.
            assertNotNull(result);
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_3)));
        }

        @Test
        @DisplayName("Filters by category")
        void findArticles_withCategoryFilter_success() {
            // given
            // 특정 카테고리만 조회하도록 요청 조건을 넣는다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setCategory(ArticleCategory.TROUBLESHOOTING);

            // when
            // 카테고리 필터가 포함된 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 조회된 문서는 모두 요청한 카테고리여야 한다.
            assertNotNull(result);
            assertTrue(result.stream().allMatch(article ->
                    article.getArticleCategory() == ArticleCategory.TROUBLESHOOTING
            ));
        }

        @Test
        @DisplayName("Sorts by view count when sort=popular")
        void findArticles_withSort_success() {
            // given
            // 인기순 정렬 조건을 요청에 넣는다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSort("popular");

            // when
            // 조회수 기준 정렬 결과를 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 앞 데이터의 조회수가 뒤 데이터보다 크거나 같아야 한다.
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getViewCount() >= result.get(i + 1).getViewCount());
            }
        }

        @Test
        @DisplayName("Filters by article title keyword")
        void findArticles_withArticleTitleKeyword_success() {
            // given
            // 제목 키워드 검색 조건을 요청에 넣는다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleTitle");
            request.setKeyword("첫번째");

            // when
            // 제목 키워드로 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 조건에 맞는 문서 한 건만 조회되어야 한다.
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Filters by author name keyword")
        void findArticles_withAuthorNameKeyword_success() {
            // given
            // 실제 직원 이름을 조회한 뒤 작성자명 검색 조건으로 사용한다.
            String otherAuthorName = jdbcTemplate.queryForObject(
                    "SELECT employee_name FROM employee WHERE employee_id = " + otherAuthorId,
                    String.class
            );
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("authorName");
            request.setKeyword(otherAuthorName);

            // when
            // 작성자명 기준으로 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 해당 작성자의 문서만 조회되는지 확인한다.
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(otherAuthorId, result.get(0).getAuthorId());
        }

        @Test
        @DisplayName("Filters by article ID keyword")
        void findArticles_withArticleIdKeyword_success() {
            // given
            // 문서 번호 검색 조건을 요청에 넣는다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleId");
            request.setKeyword(String.valueOf(TEST_ARTICLE_ID_1));
            request.setArticleIdKeyword(TEST_ARTICLE_ID_1);

            // when
            // 특정 문서 번호로 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 해당 ID의 문서 한 건만 조회되어야 한다.
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Worker can see own articles and approved articles only")
        void findArticles_withWorkerVisibility_success() {
            // given
            // WORKER 역할과 본인 ID를 넣어 권한 조건을 만든다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterId(validAuthorId);
            request.setRequesterRole("WORKER");

            // when
            // WORKER 시점에서 지식 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 본인 글은 보이고 다른 사람의 DRAFT 글은 보이지 않아야 한다.
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_4)));
        }

        @Test
        @DisplayName("Team leader cannot see draft articles")
        void findArticles_withTeamLeaderVisibility_success() {
            // given
            // TEAMLEADER 역할 기준으로 조회 요청을 만든다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterRole("TEAMLEADER");

            // when
            // 팀장 시점에서 지식 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 팀장은 DRAFT를 제외한 문서만 볼 수 있어야 한다.
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertFalse(result.stream().anyMatch(article -> article.getArticleId().equals(TEST_ARTICLE_ID_4)));
        }

        @Test
        @DisplayName("Admin can see all articles including deleted articles")
        void findArticles_withAdminVisibility_success() {
            // given
            // ADMIN 역할 기준으로 조회 요청을 만든다.
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterRole("ADMIN");

            // when
            // 관리자 시점에서 전체 목록을 조회한다.
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            // 삭제 글과 타인 DRAFT를 포함한 전체 글이 보여야 한다.
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
            // given
            // 미리 저장된 문서 ID를 상세 조회 대상으로 사용한다.
            // when
            // 해당 ID로 상세 정보를 조회한다.
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(TEST_ARTICLE_ID_1);

            // then
            // 결과가 존재하고 주요 필드가 기대값과 같아야 한다.
            assertTrue(result.isPresent());
            assertEquals(TEST_ARTICLE_ID_1, result.get().getArticleId());
            assertEquals("테스트 문서 제목 첫번째", result.get().getArticleTitle());
            assertEquals(ArticleCategory.TROUBLESHOOTING, result.get().getArticleCategory());
        }

        @Test
        @DisplayName("Returns empty when ID does not exist")
        void findArticleById_whenUnknownId_thenEmpty() {
            // given
            // 존재하지 않는 문서 ID를 조회 대상으로 사용한다.
            // when
            // 없는 ID로 상세 정보를 조회한다.
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(9999999999999L);

            // then
            // 조회 결과는 비어 있어야 한다.
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAllTags()")
    class FindAllTags {

        @Test
        @DisplayName("Returns tag list ordered by tag_name ASC")
        void findAllTags_success() {
            // given
            // setUp()에서 태그 데이터가 이미 준비되어 있다.
            // when
            // 전체 태그 목록을 조회한다.
            List<TagDto> result = knowledgeArticleMapper.findAllTags();

            // then
            // 테스트용 태그들이 결과에 포함되어야 한다.
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(tag -> tag.getTagId().equals(TEST_TAG_ID_1)));
            assertTrue(result.stream().anyMatch(tag -> tag.getTagId().equals(TEST_TAG_ID_2)));
        }

        @Test
        @DisplayName("Results are sorted by tag_name ASC")
        void findAllTags_sortedByName() {
            // given
            // 태그 목록 정렬 상태를 확인할 준비가 되어 있다.
            // when
            // 전체 태그 목록을 조회한다.
            List<TagDto> result = knowledgeArticleMapper.findAllTags();

            // then
            // 태그 이름이 오름차순으로 정렬되어야 한다.
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getTagName().compareTo(result.get(i + 1).getTagName()) <= 0);
            }
        }
    }

    @Nested
    @DisplayName("findAllEquipments()")
    class FindAllEquipments {

        @Test
        @DisplayName("Returns equipment list including test equipment")
        void findAllEquipments_success() {
            // given
            // setUp()에서 설비 데이터가 이미 준비되어 있다.
            // when
            // 전체 설비 목록을 조회한다.
            List<EquipmentDto> result = knowledgeArticleMapper.findAllEquipments();

            // then
            // 테스트용 설비가 결과에 포함되어야 한다.
            assertNotNull(result);
            assertTrue(result.stream().anyMatch(eq -> eq.getEquipmentId().equals(TEST_EQUIPMENT_ID)));
        }

        @Test
        @DisplayName("Results are sorted by equipment_name ASC")
        void findAllEquipments_sortedByName() {
            // given
            // 설비 목록 정렬 상태를 확인할 준비가 되어 있다.
            // when
            // 전체 설비 목록을 조회한다.
            List<EquipmentDto> result = knowledgeArticleMapper.findAllEquipments();

            // then
            // 설비 이름이 오름차순으로 정렬되어야 한다.
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getEquipmentName().compareTo(result.get(i + 1).getEquipmentName()) <= 0);
            }
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
                .isDeleted(isDeleted)
                .viewCount(viewCount)
                .createdAt(createdAt)
                .build();
    }
}
