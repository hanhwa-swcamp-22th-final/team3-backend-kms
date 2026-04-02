package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
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


    // 왜  mybatis에 JPA (Repository, entityManager, jdbc)가 들어갔냐면
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
    private static final Long TEST_EQUIPMENT_ID = 9000000099L;

    @BeforeEach
    void setUp() {
        // FK 체크 비활성화 후 테스트용 equipment, file_group 삽입
      // BeforeEach로 각 @Test 메서드 실행 직전마다 호출
      // file_group_id - 더미 파일 삽입하고, INSERT IGNORE - 중복이면 건너뛰고,
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
            "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
            "INSERT IGNORE INTO equipment " +
            "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
            "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-MAPPER', '매퍼테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1"); // 테스트 끝나면 원래대로 복원

        // 실제 employee ID 조회
        validAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee LIMIT 1", Long.class);
        otherAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee WHERE employee_id <> " + validAuthorId + " LIMIT 1", Long.class);
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

        KnowledgeArticle article4 = KnowledgeArticle.builder()
                .articleId(9000000000004L)
                .authorId(otherAuthorId)
                .equipmentId(validEquipmentId)
                .fileGroupId(0L)
                .articleTitle("다른 작성자의 임시 문서")
                .articleCategory(ArticleCategory.PROCESS_IMPROVEMENT)
                .articleContent("다른 작성자의 비승인 문서 본문 내용입니다.")
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .viewCount(1)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build();

        // 여기쓸려고 위에서 JPA형식 사용
        knowledgeArticleRepository.save(article1);
        knowledgeArticleRepository.save(article2);
        knowledgeArticleRepository.save(article3);
        knowledgeArticleRepository.save(article4);
        entityManager.flush(); // JPA → MyBatis 간 데이터 가시성 보장
    }

    @Nested
    // findArticles 쿼리
    @DisplayName("findArticles()")
    class FindArticles {

        @Test
        // 지식 목록 조회 성공: 전체 목록을 조회한다 (삭제된 문서 제외)
        @DisplayName("Returns list excluding deleted articles")
        void findArticles_success() {

            // given - 지식게시글 조회조건을 가진 요청 객체 생성
            ArticleQueryRequest request = new ArticleQueryRequest(); //


          // when - findArticles 호출하여 동작확인
          List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request); //

          // then - stream은 list 안에 데이터를 하나씩 꺼네서 검사하여 삭제된문서가 결과에없는지 검증
          assertNotNull(result);
          boolean deletedIncluded = result.stream()
              .anyMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_3));
          assertFalse(deletedIncluded, "삭제된 문서는 목록에 포함되지 않아야 합니다");
        }


        @Test
        // 지식 목록 조회 성공: 카테고리 필터가 반영된다
        @DisplayName("Filters by category")
        void findArticles_withCategoryFilter_success() {

          // given - 지식게시글 조회조건을 가진 요청 객체 생성
            ArticleQueryRequest request = new ArticleQueryRequest();
            // 카테고리를 트러블슈팅으로 설정
            request.setCategory(ArticleCategory.TROUBLESHOOTING);

            // when - findArticles 호출하여 동작확인
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

          // then - stream은 list 안에 데이터를 하났기 꺼네서 검사하여 삭제된문서가 결과에없는지 검증
            assertNotNull(result);
            assertTrue(result.stream()
                    .allMatch(a -> a.getArticleCategory() == ArticleCategory.TROUBLESHOOTING),
                    "조회된 모든 문서는 TROUBLESHOOTING 카테고리여야 합니다");
        }

        @Test
        // 지식 목록 조회 성공: 정렬 조건이 반영된다 (popular)
        @DisplayName("Sorts by view count when sort=popular")
        void findArticles_withSort_success() {
            // given - 조회 요청 객체 생성, 정렬 기준을 "popular"로 설정
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSort("popular");

            // when - DB에서 조회수 기준 내림차순으로 데이터 가져옴
            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            // then - 진짜 조회수가 내림차순인지 반복문으로 검증
            assertNotNull(result);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(
                        result.get(i).getViewCount() >= result.get(i + 1).getViewCount(),
                        "popular 정렬 시 조회수 내림차순이어야 합니다"
                );
            }
        }

        @Test
        @DisplayName("Filters by article title keyword")
        void findArticles_withArticleTitleKeyword_success() {
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleTitle");
            request.setKeyword("첫번째");

            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Filters by author name keyword")
        void findArticles_withAuthorNameKeyword_success() {
            String otherAuthorName = jdbcTemplate.queryForObject(
                    "SELECT employee_name FROM employee WHERE employee_id = " + otherAuthorId, String.class
            );

            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("authorName");
            request.setKeyword(otherAuthorName);

            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(otherAuthorId, result.get(0).getAuthorId());
        }

        @Test
        @DisplayName("Filters by article ID keyword")
        void findArticles_withArticleIdKeyword_success() {
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleId");
            request.setKeyword(String.valueOf(TEST_ARTICLE_ID_1));
            request.setArticleIdKeyword(TEST_ARTICLE_ID_1);

            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_ARTICLE_ID_1, result.get(0).getArticleId());
        }

        @Test
        @DisplayName("Worker can see own articles and approved articles only")
        void findArticles_withWorkerVisibility_success() {
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setRequesterId(validAuthorId);
            request.setRequesterRole("WORKER");

            List<ArticleReadDto> result = knowledgeArticleMapper.findArticles(request);

            assertNotNull(result);
            assertTrue(result.stream().anyMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_1)));
            assertTrue(result.stream().anyMatch(a -> a.getArticleId().equals(TEST_ARTICLE_ID_2)));
            assertFalse(result.stream().anyMatch(a -> a.getArticleTitle().equals("다른 작성자의 임시 문서")));
        }
    }

    @Nested
    // findArticleById 쿼리
    @DisplayName("findArticleById()")
    class FindArticleById {

        @Test
        // 지식 상세 조회 성공: 문서 상세를 조회한다
        @DisplayName("Returns article detail by ID")
        void findArticleById_success() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(TEST_ARTICLE_ID_1);

            // then
            assertTrue(result.isPresent(), "문서가 존재해야 합니다"); // 존재 확인
            assertEquals(TEST_ARTICLE_ID_1, result.get().getArticleId()); // ID 검증
            assertEquals("테스트 문서 제목 첫번째", result.get().getArticleTitle()); // 제목 검증
            assertEquals(ArticleCategory.TROUBLESHOOTING, result.get().getArticleCategory()); // 카테고리 검증
        }

        @Test
        // 지식 상세 조회 실패: 존재하지 않는 ID면 empty를 반환한다
        @DisplayName("Returns empty when ID does not exist")
        void findArticleById_whenUnknownId_thenEmpty() {
            // when
            Optional<ArticleDetailDto> result = knowledgeArticleMapper.findArticleById(9999999999999L);

            // then
            assertTrue(result.isEmpty(), "존재하지 않는 ID는 empty를 반환해야 합니다");
        }
    }
}
