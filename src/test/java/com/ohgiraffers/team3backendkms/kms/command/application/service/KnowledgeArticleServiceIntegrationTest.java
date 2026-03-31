package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
// KnowledgeArticleService 통합 테스트
@DisplayName("KnowledgeArticleService Integration Test")
class KnowledgeArticleServiceIntegrationTest {

    @Autowired
    private KnowledgeArticleService knowledgeArticleService;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TITLE   = "통합테스트 지식 문서 제목입니다";
    private static final String CONTENT = "통합테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.가나다하마바사아자퍄퍄";

    private static final Long TEST_EQUIPMENT_ID   = 9000000091L;
    private static final Long TEST_FILE_GROUP_ID  = 0L;

    private Long validAuthorId;

    @BeforeEach
    void setUpTestData() {
        // FK 체크 비활성화 후 테스트용 데이터 삽입
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
            "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
            "INSERT IGNORE INTO equipment " +
            "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
            "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-INTG', '통합테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        // 실제 employee ID 조회 (DB에 데이터 있음)
        validAuthorId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee LIMIT 1", Long.class);
    }

    @Nested
    // 지식 문서 등록 (register)
    @DisplayName("register()")
    class RegisterTest {

        @Test
        // 등록하면 PENDING 상태로 DB에 저장된다
        @DisplayName("Saves article with PENDING status in DB")
        void register_SavedAsPending() {
            // when
            Long articleId = knowledgeArticleService.register(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            assertEquals(ArticleStatus.PENDING, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    @Nested
    // 지식 문서 임시저장 (draft)
    @DisplayName("draft()")
    class DraftTest {

        @Test
        // 임시저장하면 DRAFT 상태로 DB에 저장된다
        @DisplayName("Saves article with DRAFT status in DB")
        void draft_SavedAsDraft() {
            // when
            Long articleId = knowledgeArticleService.draft(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, CONTENT);

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("조회수 증가 (incrementViewCount)")
    class IncrementViewCountTest {

        @Test
        @DisplayName("조회수가 1 증가한다")
        void incrementViewCount_IncrementsViewCount() {
            // given
            Long articleId = knowledgeArticleService.register(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            // when
            knowledgeArticleService.incrementViewCount(saved.getArticleId());

            // then
            assertEquals(1, saved.getViewCount());
        }
    }

    @Nested
    @DisplayName("지식 문서 승인 (approve)")
    class ApproveTest {

        @Test
        @DisplayName("PENDING 문서를 승인하면 APPROVED 상태로 DB에 반영된다")
        void approve_StatusChangedToApproved() {
            // given
            Long articleId = knowledgeArticleService.register(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            // when
            knowledgeArticleService.approve(saved.getArticleId(), 99L, "최종 승인합니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, saved.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("지식 문서 반려 (reject)")
    class RejectTest {

        @Test
        @DisplayName("PENDING 문서를 반려하면 REJECTED 상태와 반려 사유가 DB에 반영된다")
        void reject_StatusChangedToRejected() {
            // given
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";
            Long articleId = knowledgeArticleService.register(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            // when
            knowledgeArticleService.reject(saved.getArticleId(), reason);

            // then
            assertEquals(ArticleStatus.REJECTED, saved.getArticleStatus());
            assertEquals(reason, saved.getArticleRejectionReason());
        }
    }

    @Nested
    @DisplayName("지식 문서 삭제 (delete)")
    class DeleteTest {

        @Test
        @DisplayName("본인 DRAFT 문서를 삭제하면 isDeleted가 true로 DB에 반영된다")
        void delete_SoftDeletedInDB() {
            // given
            Long articleId = knowledgeArticleService.draft(validAuthorId, TEST_EQUIPMENT_ID, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findById(articleId).orElseThrow();

            // when
            knowledgeArticleService.delete(saved.getArticleId(), validAuthorId);

            // then
            assertTrue(saved.getIsDeleted());
        }
    }
}
