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
                .articleTitle("테스트 지식 문서 제목입니다") // 제목
                .articleCategory(ArticleCategory.TROUBLESHOOTING) // 카테고리
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.") // 내용
                .articleStatus(ArticleStatus.DRAFT) // 상태
                .isDeleted(false) // 삭제여부
                .viewCount(0) // 조회수
                .build(); // 객체 생성 완료 (builder에 값을 기반으로 KnowledgeArticle 객체를 실제로 생성)
    }

    // =========================================================
    // submit()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 제출 (submit)")
    class SubmitTest {

        @Test
        @DisplayName("DRAFT 상태의 문서를 제출하면 PENDING 상태로 바뀐다")
        void submit_Success() {
            // given
            // setUp()에서 DRAFT 상태의 article 준비됨

            // when
            article.submit();

            // then
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }

        @Test
        @DisplayName("DRAFT가 아닌 문서를 제출하면 예외가 발생한다")
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
    @DisplayName("지식 문서 승인 (approve)")
    class ApproveTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        @DisplayName("PENDING 상태의 문서를 승인하면 APPROVED로 바뀌고 승인자·승인일시가 저장된다")
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
        @DisplayName("이미 승인된 문서를 다시 승인하면 예외가 발생한다 (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            // given
            article.approve(99L, "승인합니다."); // APPROVED 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.approve(99L, "재승인 시도"));
        }

        @Test
        @DisplayName("승인 의견이 500자를 넘으면 예외가 발생한다 (APPROVAL_002)")
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
    @DisplayName("지식 문서 반려 (reject)")
    class RejectTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        @DisplayName("PENDING 상태의 문서를 반려하면 REJECTED로 바뀌고 반려 사유가 저장된다")
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
        @DisplayName("반려 사유가 10자 미만이면 예외가 발생한다 (APPROVAL_001)")
        void reject_ReasonTooShort_ThrowsException() {
            // given
            String shortReason = "짧음";

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(shortReason));
        }

        @Test
        @DisplayName("반려 사유가 500자를 넘으면 예외가 발생한다 (APPROVAL_001)")
        void reject_ReasonTooLong_ThrowsException() {
            // given
            String longReason = "a".repeat(501);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(longReason));
        }

        @Test
        @DisplayName("이미 반려된 문서를 다시 반려하면 예외가 발생한다 (APPROVAL_003)")
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
    @DisplayName("지식 문서 삭제 (softDelete)")
    class SoftDeleteTest {

        @Test
        @DisplayName("문서를 삭제하면 삭제 여부와 삭제 일시가 저장된다")
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
        @DisplayName("승인 완료된 문서를 삭제하면 예외가 발생한다 (ARTICLE_009)")
        void softDelete_Approved_ThrowsException() {
            // given
            article.submit();
            article.approve(99L, "승인합니다.");

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }

        @Test
        @DisplayName("이미 삭제된 문서를 다시 삭제하면 예외가 발생한다 (ARTICLE_008)")
        void softDelete_AlreadyDeleted_ThrowsException() {
            // given
            article.softDelete(); // 첫 번째 삭제

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }
    }
}
