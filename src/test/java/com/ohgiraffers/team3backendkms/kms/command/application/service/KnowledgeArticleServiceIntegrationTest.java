package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("KnowledgeArticleService 통합 테스트")
class KnowledgeArticleServiceIntegrationTest {

    @Autowired
    private KnowledgeArticleService knowledgeArticleService;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    private static final String TITLE   = "통합테스트 지식 문서 제목입니다";
    private static final String CONTENT = "통합테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.가나다하마바사아자퍄퍄";

    // =========================================================
    // register()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 등록 (register)")
    class RegisterTest {

        @Test
        @DisplayName("등록하면 PENDING 상태로 DB에 저장된다")
        void register_SavedAsPending() {
            // given & when
            knowledgeArticleService.register(1L, 1L, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);

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
    // draft()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 임시저장 (draft)")
    class DraftTest {

        @Test
        @DisplayName("임시저장하면 DRAFT 상태로 DB에 저장된다")
        void draft_SavedAsDraft() {
            // given & when
            knowledgeArticleService.draft(1L, 1L, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, CONTENT);

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
        }
    }

    // =========================================================
    // getDetail()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 상세 조회 (getDetail)")
    class GetDetailTest {

        @Test
        @DisplayName("조회하면 조회수가 1 증가한다")
        void getDetail_IncrementsViewCount() {
            // given
            knowledgeArticleService.register(1L, 1L, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            // when
            knowledgeArticleService.getDetail(saved.getArticleId());

            // then
            assertEquals(1, saved.getViewCount());
        }
    }

    // =========================================================
    // approve()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 승인 (approve)")
    class ApproveTest {

        @Test
        @DisplayName("PENDING 문서를 승인하면 APPROVED 상태로 DB에 반영된다")
        void approve_StatusChangedToApproved() {
            // given
            knowledgeArticleService.register(1L, 1L, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            // when
            knowledgeArticleService.approve(saved.getArticleId(), 99L, "잘 작성된 문서입니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, saved.getArticleStatus());
        }
    }

    // =========================================================
    // reject()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 반려 (reject)")
    class RejectTest {

        @Test
        @DisplayName("PENDING 문서를 반려하면 REJECTED 상태와 반려 사유가 DB에 반영된다")
        void reject_StatusChangedToRejected() {
            // given
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";
            knowledgeArticleService.register(1L, 1L, TITLE, ArticleCategory.TROUBLESHOOTING, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            // when
            knowledgeArticleService.reject(saved.getArticleId(), reason);

            // then
            assertEquals(ArticleStatus.REJECTED, saved.getArticleStatus());
            assertEquals(reason, saved.getArticleRejectionReason());
        }
    }

    // =========================================================
    // delete()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 삭제 (delete)")
    class DeleteTest {

        @Test
        @DisplayName("본인 DRAFT 문서를 삭제하면 isDeleted가 true로 DB에 반영된다")
        void delete_SoftDeletedInDB() {
            // given
            knowledgeArticleService.draft(1L, 1L, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, CONTENT);
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            // when
            knowledgeArticleService.delete(saved.getArticleId(), 1L);

            // then
            assertTrue(saved.getIsDeleted());
        }
    }
}
