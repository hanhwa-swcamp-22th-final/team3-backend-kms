package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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
    private Long validEquipmentId;

    private static final Long TEST_ARTICLE_ID_1 = 9000000000001L;
    private static final Long TEST_ARTICLE_ID_2 = 9000000000002L;
    private static final Long TEST_ARTICLE_ID_3 = 9000000000003L;
    private static final Long TEST_EQUIPMENT_ID = 9000000099L;

    @BeforeEach
    void setUp() {
        // FK 체크 비활성화 후 테스트용 equipment, file_group 삽입
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

        // 실제 employee ID 조회
        validAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee LIMIT 1", Long.class);
        validEquipmentId = TEST_EQUIPMENT_ID;

        KnowledgeArticle article1 = KnowledgeArticle.builder()
                .articleId(TEST_ARTICLE_ID_1)
                .authorId(validAuthorId)
                .equipmentId(validEquipmentId)
                .fileGroupId(0L)
                .articleTitle("테스트 문서 제목 첫번째")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("첫번째 문서 본문 내용입니다.")
                .articleStatus(ArticleStatus.APPROVED)
                .isDeleted(false)
                .viewCount(10)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        KnowledgeArticle article2 = KnowledgeArticle.builder()
                .articleId(TEST_ARTICLE_ID_2)
                .authorId(validAuthorId)
                .equipmentId(validEquipmentId)
                .fileGroupId(0L)
                .articleTitle("테스트 문서 제목 두번째")
                .articleCategory(ArticleCategory.SAFETY)
                .articleContent("두번째 문서 본문 내용입니다.")
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(false)
                .viewCount(5)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        KnowledgeArticle article3 = KnowledgeArticle.builder()
                .articleId(TEST_ARTICLE_ID_3)
                .authorId(validAuthorId)
                .equipmentId(validEquipmentId)
                .fileGroupId(0L)
                .articleTitle("삭제된 문서 제목")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("삭제된 문서 본문 내용입니다.")
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(true)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        knowledgeArticleRepository.save(article1);
        knowledgeArticleRepository.save(article2);
        knowledgeArticleRepository.save(article3);
        entityManager.flush(); // JPA → MyBatis 간 데이터 가시성 보장
    }

    @Nested
    @DisplayName("findArticles 쿼리")
    class FindArticles {

        @Test
        @DisplayName("지식 목록 조회 성공: 전체 목록을 조회한다 (삭제된 문서 제외)")
        void findArticles_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            boolean deletedIncluded = result.stream()
                    .anyMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_3));
            assertFalse(deletedIncluded, "삭제된 문서는 목록에 포함되지 않아야 합니다");
        }

        @Test
        @DisplayName("지식 목록 조회 성공: 카테고리 필터가 반영된다")
        void findArticles_withCategoryFilter_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setCategory(ArticleCategory.TROUBLESHOOTING);

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.stream()
                    .allMatch(a -> a.getArticleCategory() == ArticleCategory.TROUBLESHOOTING),
                    "조회된 모든 문서는 TROUBLESHOOTING 카테고리여야 합니다");
        }

        @Test
        @DisplayName("지식 목록 조회 성공: 정렬 조건이 반영된다 (popular)")
        void findArticles_withSort_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSort("popular");

            // when
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(
                        result.get(i).getViewCount() >= result.get(i + 1).getViewCount(),
                        "popular 정렬 시 조회수 내림차순이어야 합니다"
                );
            }
        }
    }

    @Nested
    @DisplayName("findArticleById 쿼리")
    class FindArticleById {

        @Test
        @DisplayName("지식 상세 조회 성공: 문서 상세를 조회한다")
        void findArticleById_success() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(TEST_ARTICLE_ID_1);

            // then
            assertTrue(result.isPresent(), "문서가 존재해야 합니다");
            assertEquals(TEST_ARTICLE_ID_1, result.get().getArticleId());
            assertEquals("테스트 문서 제목 첫번째", result.get().getArticleTitle());
            assertEquals(ArticleCategory.TROUBLESHOOTING, result.get().getArticleCategory());
        }

        @Test
        @DisplayName("지식 상세 조회 실패: 존재하지 않는 ID면 empty를 반환한다")
        void findArticleById_whenUnknownId_thenEmpty() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(9999999999999L);

            // then
            assertTrue(result.isEmpty(), "존재하지 않는 ID는 empty를 반환해야 합니다");
        }
    }
}
