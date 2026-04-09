package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmark;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmarkId;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeBookmarkRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 북마크 API 통합 테스트
 * - 실제 DB 연동
 * - 북마크 추가 / 취소 / 내 목록 조회 검증
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class WorkerBookmarkControllerIntegrationTest {

    private static final String BASE_URL = "/api/kms/bookmarks";
    private static final String MY_URL = "/api/kms/my/bookmarks";

    // 테스트에 사용할 직원 ID (북마크를 한 직원)
    private static final Long EMPLOYEE_ID = 1774942559890303L;
    // 테스트에 사용할 설비 ID
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    // 게시글 본문
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository articleRepository;

    @Autowired
    private KnowledgeBookmarkRepository bookmarkRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // FK 제약 우회 후 필수 보조 데이터 삽입
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    // =====================================================
    // POST /api/kms/bookmarks — 북마크 추가
    // =====================================================

    @Test
    @DisplayName("Add bookmark API integration success: bookmark saved to DB")
    void addBookmark_Success() throws Exception {
        // given — 북마크할 게시글 저장
        KnowledgeArticle article = saveArticle();

        Map<String, Object> request = Map.of(
                "articleId", article.getArticleId(),
                "employeeId", EMPLOYEE_ID
        );

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // DB에 실제로 저장됐는지 검증
        flushAndClear();
        KnowledgeBookmarkId id = new KnowledgeBookmarkId(article.getArticleId(), EMPLOYEE_ID);
        assert bookmarkRepository.existsById(id);
    }

    @Test
    @DisplayName("Add bookmark API integration fail: duplicate bookmark returns 409")
    void addBookmark_AlreadyExists() throws Exception {
        // given — 게시글 저장 후 북마크 미리 등록
        KnowledgeArticle article = saveArticle();
        saveBookmark(article.getArticleId(), EMPLOYEE_ID);

        Map<String, Object> request = Map.of(
                "articleId", article.getArticleId(),
                "employeeId", EMPLOYEE_ID
        );

        // when & then — 중복 북마크 시 409 응답
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // DELETE /api/kms/bookmarks — 북마크 취소
    // =====================================================

    @Test
    @DisplayName("Remove bookmark API integration success: bookmark deleted from DB")
    void removeBookmark_Success() throws Exception {
        // given — 게시글 저장 후 북마크 미리 등록
        KnowledgeArticle article = saveArticle();
        saveBookmark(article.getArticleId(), EMPLOYEE_ID);

        // when & then
        mockMvc.perform(delete(BASE_URL)
                        .param("articleId", String.valueOf(article.getArticleId()))
                        .param("employeeId", String.valueOf(EMPLOYEE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // DB에서 실제로 삭제됐는지 검증
        flushAndClear();
        KnowledgeBookmarkId id = new KnowledgeBookmarkId(article.getArticleId(), EMPLOYEE_ID);
        assert !bookmarkRepository.existsById(id);
    }

    @Test
    @DisplayName("Remove bookmark API integration fail: not found bookmark returns 404")
    void removeBookmark_NotFound() throws Exception {
        // given — 북마크 없이 취소 요청
        KnowledgeArticle article = saveArticle();

        // when & then — 없는 북마크 취소 시 404 응답
        mockMvc.perform(delete(BASE_URL)
                        .param("articleId", String.valueOf(article.getArticleId()))
                        .param("employeeId", String.valueOf(EMPLOYEE_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // GET /api/kms/my/bookmarks — 내 북마크 목록 조회
    // =====================================================

    @Test
    @DisplayName("Get my bookmarks API integration success: returns bookmarked articles")
    void getMyBookmarks_Success() throws Exception {
        // given — 게시글 저장 후 북마크 등록
        KnowledgeArticle article = saveArticle();
        saveBookmark(article.getArticleId(), EMPLOYEE_ID);
        flushAndClear();

        // when & then
        mockMvc.perform(get(MY_URL)
                        .param("employeeId", String.valueOf(EMPLOYEE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].articleId").value(article.getArticleId()));
    }

    @Test
    @DisplayName("Get my bookmarks API integration success: returns empty list when no bookmarks")
    void getMyBookmarks_Empty() throws Exception {
        // given — 북마크 없음

        // when & then
        mockMvc.perform(get(MY_URL)
                        .param("employeeId", String.valueOf(EMPLOYEE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =====================================================
    // 헬퍼 메서드
    // =====================================================

    // 테스트용 게시글 저장
    private KnowledgeArticle saveArticle() {
        return articleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(EMPLOYEE_ID)
                .equipmentId(EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle("북마크 통합 테스트용 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.APPROVED)
                .approvalVersion(1)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }

    // 테스트용 북마크 저장
    private void saveBookmark(Long articleId, Long employeeId) {
        bookmarkRepository.save(KnowledgeBookmark.builder()
                .id(new KnowledgeBookmarkId(articleId, employeeId))
                .build());
    }

    // JPA 1차 캐시를 비워 DB 최신 상태를 조회
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
