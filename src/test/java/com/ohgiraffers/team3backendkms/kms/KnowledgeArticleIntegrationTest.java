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
@DisplayName("KnowledgeArticle 전체 통합 테스트 (5-5)")
class KnowledgeArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 테스트용 상수
    private static final String TITLE   = "통합테스트 지식 문서 제목입니다";
    private static final String CONTENT = "통합테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.가나다라마바사";
    private static final Long   TEST_EQUIPMENT_ID = 9000000092L;

    // 인증 사용자 (WORKER — 등록, 임시저장, 삭제)
    protected CustomUserDetails workerUser;

    // 인증 사용자 (TL — 승인, 반려)
    protected CustomUserDetails tlUser;

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
    }

    // =========================================================
    // 공통 헬퍼
    // =========================================================

    private KnowledgeArticle savePendingArticle() {
        knowledgeArticleRepository.save(
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
        return knowledgeArticleRepository.findAll().stream()
                .filter(a -> TITLE.equals(a.getArticleTitle()))
                .findFirst()
                .orElseThrow();
    }

    // =========================================================
    // POST /api/kms/articles
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles — 지식 문서 등록")
    class Register {

        @Test
        @DisplayName("정상 요청 시 200 OK 응답과 함께 DB에 PENDING 상태로 저장된다")
        void register_savedAsPending() throws Exception {
            // given
            ArticleRegisterRequest request = new ArticleRegisterRequest(
                    validAuthorId, TITLE, ArticleCategory.TROUBLESHOOTING, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(ArticleStatus.PENDING, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // POST /api/kms/articles/drafts
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles/drafts — 지식 문서 임시저장")
    class Draft {

        @Test
        @DisplayName("정상 요청 시 200 OK 응답과 함께 DB에 DRAFT 상태로 저장된다")
        void draft_savedAsDraft() throws Exception {
            // given
            ArticleDraftRequest request = new ArticleDraftRequest(
                    validAuthorId, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            mockMvc.perform(post("/api/kms/articles/drafts")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // POST /api/kms/approval/{articleId}/approve
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/approval/{articleId}/approve — 지식 문서 승인")
    class Approve {

        @Test
        @DisplayName("PENDING 문서 승인 시 200 OK 응답과 함께 DB에 APPROVED 상태로 반영된다")
        void approve_statusChangedToApproved() throws Exception {
            // given
            KnowledgeArticle saved = savePendingArticle();
            ArticleApproveRequest request = new ArticleApproveRequest(validAuthorId, "잘 작성된 문서입니다.");

            // when
            mockMvc.perform(post("/api/kms/approval/" + saved.getArticleId() + "/approve")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
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
    @DisplayName("POST /api/kms/approval/{articleId}/reject — 지식 문서 반려")
    class Reject {

        @Test
        @DisplayName("PENDING 문서 반려 시 200 OK 응답과 함께 DB에 REJECTED 상태와 반려 사유가 반영된다")
        void reject_statusChangedToRejected() throws Exception {
            // given
            KnowledgeArticle saved = savePendingArticle();
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";
            ArticleRejectRequest request = new ArticleRejectRequest(reason);

            // when
            mockMvc.perform(post("/api/kms/approval/" + saved.getArticleId() + "/reject")
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
    @DisplayName("DELETE /api/kms/articles/{articleId} — 지식 문서 삭제")
    class Delete {

        @Test
        @DisplayName("본인 DRAFT 문서 삭제 시 200 OK 응답과 함께 DB에 isDeleted=true로 반영된다")
        void delete_softDeletedInDB() throws Exception {
            // given
            knowledgeArticleRepository.save(
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
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

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
}
