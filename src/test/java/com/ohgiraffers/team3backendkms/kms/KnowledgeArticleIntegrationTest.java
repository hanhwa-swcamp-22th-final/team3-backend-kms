package com.ohgiraffers.team3backendkms.kms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.config.security.CustomUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApproveRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// KnowledgeArticle 전체 통합 테스트
@DisplayName("KnowledgeArticle Integration Test")
class KnowledgeArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    // 테스트용 상수
    private static final String TITLE   = "통합테스트 지식 문서 제목입니다";
    private static final String CONTENT = "통합테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.가나다라마바사";
    private static final Long   TEST_EQUIPMENT_ID = 9000000092L;

    // 인증 사용자 (WORKER — 등록, 임시저장, 삭제)
    protected CustomUserDetails workerUser;

    // 인증 사용자 (TL — 1차 승인, 반려)
    protected CustomUserDetails tlUser;

    // 인증 사용자 (DL — 최종 승인, 반려)
    protected CustomUserDetails dlUser;

    private Long validAuthorId;

    @BeforeEach
    void setUpTestData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
            "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
            "INSERT IGNORE INTO equipment " +
            "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
            "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-INTG2', '전체통합테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        validAuthorId = jdbcTemplate.queryForObject(
            "SELECT employee_id FROM employee LIMIT 1", Long.class);

        workerUser = new CustomUserDetails(
            "WORKER_TEST", "pw",
            List.of(new SimpleGrantedAuthority("WORKER")),
            validAuthorId
        );

        tlUser = new CustomUserDetails(
            "TL_TEST", "pw",
            List.of(new SimpleGrantedAuthority("TL")),
            validAuthorId
        );

        dlUser = new CustomUserDetails(
            "DL_TEST", "pw",
            List.of(new SimpleGrantedAuthority("DL")),
            validAuthorId
        );
    }

    // =========================================================
    // 공통 헬퍼
    // =========================================================

    private KnowledgeArticle savePendingArticle() {
        return knowledgeArticleRepository.save(
                KnowledgeArticle.builder()
                        .articleId(new TimeBasedIdGenerator().generate())
                        .authorId(validAuthorId)
                        .equipmentId(TEST_EQUIPMENT_ID)
                        .fileGroupId(0L)
                        .articleTitle(TITLE)
                        .articleCategory(ArticleCategory.TROUBLESHOOTING)
                        .articleContent(CONTENT)
                        .articleStatus(ArticleStatus.PENDING)
                        .isDeleted(false)
                        .viewCount(0)
                        .build()
        );
    }

    // =========================================================
    // POST /api/kms/articles
    // =========================================================

    @Nested
    // POST /api/kms/articles — 지식 문서 등록
    @DisplayName("POST /api/kms/articles")
    class Register {

        @Test
        // 정상 요청 시 200 OK 응답과 함께 DB에 PENDING 상태로 저장된다
        @DisplayName("Returns 200 OK and saves article with PENDING status in DB")
        void register_savedAsPending() throws Exception {
            // given
            ArticleRegisterRequest request = new ArticleRegisterRequest(
                    validAuthorId, TITLE, ArticleCategory.TROUBLESHOOTING, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            String response = mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn().getResponse().getContentAsString();

            // then
            Long articleId = objectMapper.readTree(response).get("data").asLong();
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            assertEquals(ArticleStatus.PENDING, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // POST /api/kms/articles/drafts
    // =========================================================

    @Nested
    // POST /api/kms/articles/drafts — 지식 문서 임시저장
    @DisplayName("POST /api/kms/articles/drafts")
    class Draft {

        @Test
        // 정상 요청 시 200 OK 응답과 함께 DB에 DRAFT 상태로 저장된다
        @DisplayName("Returns 200 OK and saves article with DRAFT status in DB")
        void draft_savedAsDraft() throws Exception {
            // given
            ArticleDraftRequest request = new ArticleDraftRequest(
                    validAuthorId, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            String response = mockMvc.perform(post("/api/kms/articles/drafts")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn().getResponse().getContentAsString();

            // then
            Long articleId = objectMapper.readTree(response).get("data").asLong();
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // POST /api/kms/approval/{articleId}/approve
    // =========================================================

    @Nested
    // POST /api/kms/approval/{articleId}/approve — DL 최종 승인
    @DisplayName("POST /api/kms/approval/{articleId}/approve")
    class Approve {

        @Test
        // TL 1차 승인 후 DL 최종 승인 시 200 OK 응답과 함께 DB에 APPROVED 상태로 반영된다
        @DisplayName("Returns 200 OK and changes status to APPROVED in DB")
        void approve_statusChangedToApproved() throws Exception {
            // given
            KnowledgeArticle saved = savePendingArticle();

            // TL 1차 승인 (PENDING → TL_APPROVED)
            mockMvc.perform(post("/api/kms/approval/" + saved.getArticleId() + "/tl-approve")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ArticleApproveRequest(validAuthorId, "1차 검토 완료입니다."))))
                    .andExpect(status().isOk());

            // DL 최종 승인
            mockMvc.perform(post("/api/kms/approval/" + saved.getArticleId() + "/approve")
                            .with(user(dlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ArticleApproveRequest(validAuthorId, "최종 승인합니다."))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            assertEquals(ArticleStatus.APPROVED, saved.getArticleStatus());
        }
    }

    // =========================================================
    // POST /api/kms/approval/{articleId}/reject
    // =========================================================

    @Nested
    // POST /api/kms/approval/{articleId}/tl-reject — TL 반려
    @DisplayName("POST /api/kms/approval/{articleId}/tl-reject")
    class Reject {

        @Test
        // PENDING 문서 TL 반려 시 200 OK 응답과 함께 DB에 REJECTED 상태와 반려 사유가 반영된다
        @DisplayName("Returns 200 OK and changes status to REJECTED with rejection reason in DB")
        void reject_statusChangedToRejected() throws Exception {
            // given
            KnowledgeArticle saved = savePendingArticle();
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";
            ArticleRejectRequest request = new ArticleRejectRequest(reason);

            // when
            mockMvc.perform(post("/api/kms/approval/" + saved.getArticleId() + "/tl-reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            assertEquals(ArticleStatus.REJECTED, saved.getArticleStatus());
            assertEquals(reason, saved.getArticleRejectionReason());
        }
    }

    // =========================================================
    // DELETE /api/kms/articles/{articleId}
    // =========================================================

    @Nested
    // DELETE /api/kms/articles/{articleId} — 지식 문서 삭제
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class Delete {

        @Test
        // 본인 DRAFT 문서 삭제 시 200 OK 응답과 함께 DB에 isDeleted=true로 반영된다
        @DisplayName("Returns 200 OK and sets isDeleted to true in DB")
        void delete_softDeletedInDB() throws Exception {
            // given
            KnowledgeArticle saved = knowledgeArticleRepository.save(
                    KnowledgeArticle.builder()
                            .articleId(new TimeBasedIdGenerator().generate())
                            .authorId(validAuthorId)
                            .equipmentId(TEST_EQUIPMENT_ID)
                            .fileGroupId(0L)
                            .articleTitle(TITLE)
                            .articleCategory(ArticleCategory.TROUBLESHOOTING)
                            .articleContent(CONTENT)
                            .articleStatus(ArticleStatus.DRAFT)
                            .isDeleted(false)
                            .viewCount(0)
                            .build()
            );

            // when
            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .param("requesterId", String.valueOf(validAuthorId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            assertTrue(saved.getIsDeleted());
        }
    }

    // =========================================================
    // GET /api/kms/articles
    // =========================================================

    @Nested
    // GET /api/kms/articles — 지식 문서 목록 조회
    @DisplayName("GET /api/kms/articles")
    class GetArticles {

        @Test
        // 문서가 존재할 때 200 OK 응답과 함께 목록 JSON을 반환한다
        @DisplayName("Returns 200 OK with list JSON")
        void getArticles_returnsList() throws Exception {
            // given
            savePendingArticle();

            // when & then
            mockMvc.perform(get("/api/kms/articles")
                            .with(user(workerUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // =========================================================
    // GET /api/kms/articles/{articleId}
    // =========================================================

    @Nested
    // GET /api/kms/articles/{articleId} — 지식 문서 상세 조회
    @DisplayName("GET /api/kms/articles/{articleId}")
    class GetArticleDetail {

        @Test
        // 존재하는 문서 조회 시 200 OK 응답과 함께 상세 JSON을 반환한다
        @DisplayName("Returns 200 OK with detail JSON")
        void getArticleDetail_returnsDetail() throws Exception {
            // given
            KnowledgeArticle saved = savePendingArticle();
            entityManager.flush(); // JPA → DB 반영 후 MyBatis가 읽을 수 있도록

            // when & then
            mockMvc.perform(get("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleId").value(saved.getArticleId()))
                    .andExpect(jsonPath("$.data.articleTitle").value(TITLE));
        }

        @Test
        // 존재하지 않는 문서 조회 시 404 응답을 반환한다
        @DisplayName("Returns 404 when article does not exist")
        void getArticleDetail_whenNotFound_returns404() throws Exception {
            // given
            Long notExistId = 999L;

            // when & then
            mockMvc.perform(get("/api/kms/articles/" + notExistId)
                            .with(user(workerUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
        }
    }
}
