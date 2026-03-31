package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeArticleTest {

    private KnowledgeArticle article;

    @BeforeEach // 밑에 실행전마다 반복됨
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
    // 지식 문서 제출 (submit)
    @DisplayName("submit()")
    class SubmitTest {

        @Test
        // DRAFT 상태의 문서를 제출하면 PENDING 상태로 바뀐다
        @DisplayName("Changes status from DRAFT to PENDING")
        void submit_Success() {
            // given
            // setUp()에서 DRAFT 상태의 article 준비됨

            // when
            article.submit();

            // then
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }

        @Test
        // DRAFT가 아닌 문서를 제출하면 예외가 발생한다
        @DisplayName("Throws exception when status is not DRAFT")
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
    // 지식 문서 승인 (approve) — TL 또는 DL
    @DisplayName("approve()")
    class ApproveTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        // PENDING 상태의 문서를 승인하면 APPROVED로 바뀌고 승인자·승인일시가 저장된다
        @DisplayName("Changes status to APPROVED and saves approver info")
        void approve_Success() {
            // given
            Long approverId = 99L;
            String opinion = "최종 승인합니다.";

            // when
            article.approve(approverId, opinion);

            // then
            assertEquals(ArticleStatus.APPROVED, article.getArticleStatus());
            assertEquals(approverId, article.getApprovedBy());
            assertEquals(opinion, article.getArticleApprovalOpinion());
            assertNotNull(article.getApprovedAt());
        }

        @Test
        // 이미 승인된 문서를 다시 승인하면 예외가 발생한다 (APPROVAL_005)
        @DisplayName("Throws exception when status is APPROVED (APPROVAL_005)")
        void approve_AlreadyApproved_ThrowsException() {
            // given
            article.approve(99L, "최종 승인합니다."); // APPROVED 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.approve(99L, "재승인 시도"));
        }

        @Test
        // 반려된 문서를 승인하면 예외가 발생한다 (APPROVAL_006)
        @DisplayName("Throws exception when status is REJECTED (APPROVAL_006)")
        void approve_WhenRejected_ThrowsException() {
            // given
            article.reject("내용이 충분하지 않습니다. 보완 후 재제출해주세요.");

            // when & then
            assertThrows(IllegalStateException.class, () -> article.approve(99L, "잘못된 승인 시도"));
        }

        @Test
        // 삭제된 문서를 승인하면 예외가 발생한다 (ARTICLE_008)
        @DisplayName("Throws exception when article is deleted (ARTICLE_008)")
        void approve_WhenDeleted_ThrowsException() {
            // given — PENDING 상태에서 삭제 불가이므로 DRAFT로 시작해 삭제 후 PENDING으로 상태 변경
            KnowledgeArticle deletedArticle = KnowledgeArticle.builder()
                    .authorId(1L)
                    .articleStatus(ArticleStatus.PENDING)
                    .isDeleted(true)
                    .viewCount(0)
                    .build();

            // when & then
            assertThrows(IllegalStateException.class, () -> deletedArticle.approve(99L, "승인 시도"));
        }

        @Test
        // 승인 의견이 500자를 넘으면 예외가 발생한다 (APPROVAL_002)
        @DisplayName("Throws exception when opinion exceeds 500 characters (APPROVAL_002)")
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
    // 지식 문서 반려 (reject)
    @DisplayName("reject()")
    class RejectTest {

        @BeforeEach
        void setUpPending() {
            article.submit(); // PENDING 상태로 전환
        }

        @Test
        // PENDING 상태의 문서를 반려하면 REJECTED로 바뀌고 반려 사유가 저장된다
        @DisplayName("Changes status to REJECTED and saves rejection reason")
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
        // 반려 사유가 10자 미만이면 예외가 발생한다 (APPROVAL_001)
        @DisplayName("Throws exception when reason is less than 10 characters (APPROVAL_001)")
        void reject_ReasonTooShort_ThrowsException() {
            // given
            String shortReason = "짧음";

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(shortReason));
        }

        @Test
        // 반려 사유가 500자를 넘으면 예외가 발생한다 (APPROVAL_001)
        @DisplayName("Throws exception when reason exceeds 500 characters (APPROVAL_001)")
        void reject_ReasonTooLong_ThrowsException() {
            // given
            String longReason = "a".repeat(501);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> article.reject(longReason));
        }

        @Test
        // 이미 반려된 문서를 다시 반려하면 예외가 발생한다 (APPROVAL_007)
        @DisplayName("Throws exception when status is REJECTED (APPROVAL_007)")
        void reject_AlreadyRejected_ThrowsException() {
            // given
            article.reject("내용이 충분하지 않습니다. 보완 후 재제출해주세요."); // REJECTED 상태로 전환

            // when & then
            assertThrows(IllegalStateException.class, () -> article.reject("재반려 시도입니다."));
        }

        @Test
        // 승인 완료된 문서를 반려하면 예외가 발생한다 (APPROVAL_008)
        @DisplayName("Throws exception when status is APPROVED (APPROVAL_008)")
        void reject_WhenApproved_ThrowsException() {
            // given
            article.approve(99L, "최종 승인합니다.");

            // when & then
            assertThrows(IllegalStateException.class, () -> article.reject("반려 시도입니다. 보완해주세요."));
        }

        @Test
        // 삭제된 문서를 반려하면 예외가 발생한다 (ARTICLE_008)
        @DisplayName("Throws exception when article is deleted (ARTICLE_008)")
        void reject_WhenDeleted_ThrowsException() {
            // given
            KnowledgeArticle deletedArticle = KnowledgeArticle.builder()
                    .authorId(1L)
                    .articleStatus(ArticleStatus.PENDING)
                    .isDeleted(true)
                    .viewCount(0)
                    .build();

            // when & then
            assertThrows(IllegalStateException.class, () -> deletedArticle.reject("반려 시도입니다. 보완해주세요."));
        }
    }

    // =========================================================
    // softDelete()
    // =========================================================

    @Nested
    // 지식 문서 삭제 (softDelete)
    @DisplayName("softDelete()")
    class SoftDeleteTest {

        @Test
        // 문서를 삭제하면 삭제 여부와 삭제 일시가 저장된다
        @DisplayName("Sets isDeleted to true and saves deleted time")
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
        // 승인 완료된 문서를 삭제하면 예외가 발생한다 (ARTICLE_009)
        @DisplayName("Throws exception when status is APPROVED (ARTICLE_009)")
        void softDelete_Approved_ThrowsException() {
            // given
            article.submit();
            article.approve(99L, "최종 승인합니다.");

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }

        @Test
        // 이미 삭제된 문서를 다시 삭제하면 예외가 발생한다 (ARTICLE_008)
        @DisplayName("Throws exception when already deleted (ARTICLE_008)")
        void softDelete_AlreadyDeleted_ThrowsException() {
            // given
            article.softDelete(); // 첫 번째 삭제

            // when & then
            assertThrows(IllegalStateException.class, () -> article.softDelete());
        }
    }
}
