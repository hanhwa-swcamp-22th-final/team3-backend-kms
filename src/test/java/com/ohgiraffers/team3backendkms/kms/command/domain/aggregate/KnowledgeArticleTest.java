package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeArticleTest {

    private KnowledgeArticle article;

    @BeforeEach
    void setUp() {
        article = KnowledgeArticle.builder()
                .authorId(1L)
                .articleTitle("테스트 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.장애조치)
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    // =========================================================
    // submit()
    // =========================================================

    @Nested
    @DisplayName("submit() - 지식 문서 제출")
    class SubmitTest {

        @Test
        @DisplayName("DRAFT 상태에서 submit() 호출 시 PENDING으로 변경된다")
        void submit_Success() {
            // given
            // setUp()에서 DRAFT 상태의 article 준비됨

            // when
            article.submit();

            // then
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }

        @Test
        @DisplayName("DRAFT가 아닌 상태에서 submit() 호출 시 IllegalStateException이 발생한다")
        void submit_NotDraft_ThrowsException() {
            // given
            article.submit(); // PENDING 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.submit());
        }
    }

    // =========================================================
    // approve()
    // =========================================================

    @Nested
    @DisplayName("approve() - 지식 문서 승인")
    class ApproveTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        @DisplayName("PENDING 상태에서 approve() 호출 시 APPROVED로 변경되고 승인자·승인일시가 저장된다")
        void approve_Success() {
            // given
            Long approvedBy = 99L;
            String opinion = "잘 작성된 문서입니다.";

            // when
            article.approve(approvedBy, opinion);

            // then
            assertEquals(ArticleStatus.APPROVED, article.getArticleStatus());
            assertEquals(approvedBy, article.getApprovedBy());
            assertEquals(opinion, article.getArticleApprovalOpinion());
            assertNotNull(article.getApprovedAt());
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 approve() 호출 시 IllegalStateException이 발생한다 (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            // given
            article.approve(99L, "승인합니다."); // APPROVED 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.approve(99L, "재승인 시도"));
        }

        @Test
        @DisplayName("승인 의견이 500자를 초과하면 IllegalArgumentException이 발생한다 (APPROVAL_002)")
        void approve_OpinionTooLong_ThrowsException() {
            // given
            String longOpinion = "a".repeat(501);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.approve(99L, longOpinion));
        }
    }

    // =========================================================
    // reject()
    // =========================================================

    @Nested
    @DisplayName("reject() - 지식 문서 반려")
    class RejectTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        @DisplayName("PENDING 상태에서 reject() 호출 시 REJECTED로 변경되고 반려 사유가 저장된다")
        void reject_Success() {
            // given
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";

            // when
            article.reject(reason);

            // then
            assertEquals(ArticleStatus.REJECTED, article.getArticleStatus());
            assertEquals(reason, article.getArticleRejectionReason());
        }

        @Test
        @DisplayName("반려 사유가 10자 미만이면 IllegalArgumentException이 발생한다 (APPROVAL_001)")
        void reject_ReasonTooShort_ThrowsException() {
            // given
            String shortReason = "짧음";

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(shortReason));
        }

        @Test
        @DisplayName("반려 사유가 500자를 초과하면 IllegalArgumentException이 발생한다 (APPROVAL_001)")
        void reject_ReasonTooLong_ThrowsException() {
            // given
            String longReason = "a".repeat(501);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(longReason));
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 reject() 호출 시 IllegalStateException이 발생한다 (APPROVAL_003)")
        void reject_NotPending_ThrowsException() {
            // given
            article.reject("내용이 충분하지 않습니다. 보완 후 재제출해주세요."); // REJECTED 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.reject("재반려 시도입니다."));
        }
    }

    // =========================================================
    // softDelete()
    // =========================================================

    @Nested
    @DisplayName("softDelete() - 지식 문서 소프트 딜리트")
    class SoftDeleteTest {

        @Test
        @DisplayName("softDelete() 호출 시 is_deleted=true, deletedAt이 저장된다")
        void softDelete_Success() {
            // given
            // setUp()에서 DRAFT 상태의 article 준비됨

            // when
            article.softDelete();

            // then
            assertTrue(article.getIsDeleted());
            assertNotNull(article.getDeletedAt());
        }

        @Test
        @DisplayName("APPROVED 상태의 문서를 직접 삭제하면 IllegalStateException이 발생한다 (ARTICLE_009)")
        void softDelete_Approved_ThrowsException() {
            // given
            article.submit();
            article.approve(99L, "승인합니다.");

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }

        @Test
        @DisplayName("이미 삭제된 문서를 재삭제하면 IllegalStateException이 발생한다 (ARTICLE_008)")
        void softDelete_AlreadyDeleted_ThrowsException() {
            // given
            article.softDelete(); // 첫 번째 삭제

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }
    }
}
