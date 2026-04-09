package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KnowledgeArticle")
class KnowledgeArticleTest {

    private KnowledgeArticle buildArticle(ArticleStatus status, Integer viewCount) {
        return KnowledgeArticle.builder()
                .articleId(1L)
                .authorId(10L)
                .equipmentId(100L)
                .fileGroupId(0L)
                .articleTitle("기본 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("기본 본문 내용입니다. 도메인 테스트를 위해 충분한 길이의 문장을 작성합니다.")
                .articleStatus(status)
                .approvalVersion(status == ArticleStatus.APPROVED ? 1 : 0)
                .isDeleted(false)
                .viewCount(viewCount)
                .build();
    }

    @Nested
    @DisplayName("submit()")
    class SubmitTest {

        @Test
        @DisplayName("Changes status to PENDING")
        void submit_ChangesStatusToPending() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.DRAFT, 0);

            // when
            article.submit();

            // then
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("approve()")
    class ApproveTest {

        @Test
        @DisplayName("Changes status to APPROVED and stores approval info")
        void approve_ChangesStatusAndStoresApprovalInfo() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING, 0);

            // when
            article.approve(20L, "승인합니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, article.getArticleStatus());
            assertEquals(20L, article.getApprovedBy());
            assertEquals("승인합니다.", article.getArticleApprovalOpinion());
            assertNotNull(article.getApprovedAt());
            assertEquals(1, article.getApprovalVersion());
        }
    }

    @Nested
    @DisplayName("createRevisionCopy()")
    class CreateRevisionCopyTest {

        @Test
        @DisplayName("Creates revision copy without changing original")
        void createRevisionCopy_CreatesDraftCopy() {
            // given
            KnowledgeArticle original = buildArticle(ArticleStatus.APPROVED, 0);

            // when
            KnowledgeArticle revision = KnowledgeArticle.createRevisionCopy(2L, original);

            // then
            assertEquals(2L, revision.getArticleId());
            assertEquals(original.getArticleId(), revision.getOriginalArticleId());
            assertEquals(ArticleStatus.DRAFT, revision.getArticleStatus());
            assertEquals(original.getArticleTitle(), revision.getArticleTitle());
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTest {

        @Test
        @DisplayName("Changes status to REJECTED and stores review comment")
        void reject_ChangesStatusAndStoresReviewComment() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING, 0);

            // when
            article.reject(99L, "반려 사유를 남깁니다.");

            // then
            assertEquals(ArticleStatus.REJECTED, article.getArticleStatus());
            assertEquals(99L, article.getApprovedBy());
            assertEquals("반려 사유를 남깁니다.", article.getArticleRejectionReason());
        }
    }

    @Nested
    @DisplayName("updateDraft()")
    class UpdateTest {

        @Test
        @DisplayName("Updates fields and keeps status as DRAFT")
        void updateDraft_UpdatesFieldsAndKeepsStatusAsDraft() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.DRAFT, 0);

            // when
            article.updateDraft(
                    "수정된 제목입니다",
                    ArticleCategory.PROCESS_IMPROVEMENT,
                    200L,
                    "수정된 본문 내용입니다. 도메인 단위 테스트를 위해 충분한 길이의 본문을 작성합니다."
            );

            // then
            assertEquals("수정된 제목입니다", article.getArticleTitle());
            assertEquals(ArticleCategory.PROCESS_IMPROVEMENT, article.getArticleCategory());
            assertEquals(200L, article.getEquipmentId());
            assertEquals("수정된 본문 내용입니다. 도메인 단위 테스트를 위해 충분한 길이의 본문을 작성합니다.", article.getArticleContent());
            assertEquals(ArticleStatus.DRAFT, article.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("incrementViewCount()")
    class IncrementViewCountTest {

        @Test
        @DisplayName("Increments view count by 1 when count exists")
        void incrementViewCount_IncrementsExistingCount() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.APPROVED, 3);

            // when
            article.incrementViewCount();

            // then
            assertEquals(4, article.getViewCount());
        }

        @Test
        @DisplayName("Initializes to 1 when view count is null")
        void incrementViewCount_InitializesWhenNull() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.APPROVED, null);

            // when
            article.incrementViewCount();

            // then
            assertEquals(1, article.getViewCount());
        }
    }

    @Nested
    @DisplayName("softDelete()")
    class SoftDeleteTest {

        @Test
        @DisplayName("Sets isDeleted to true and stores deletedAt")
        void softDelete_SetsDeletedFields() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.DRAFT, 0);

            // when
            article.softDelete();

            // then
            assertTrue(article.getIsDeleted());
            assertNotNull(article.getDeletedAt());
        }
    }

    @Nested
    @DisplayName("adminDelete()")
    class AdminDeleteTest {

        @Test
        @DisplayName("Sets isDeleted to true and stores deletion reason")
        void adminDelete_SetsDeletedFieldsAndReason() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.APPROVED, 0);

            // when
            article.adminDelete("관리자 삭제 사유입니다.");

            // then
            assertTrue(article.getIsDeleted());
            assertNotNull(article.getDeletedAt());
            assertEquals("관리자 삭제 사유입니다.", article.getArticleDeletionReason());
        }
    }

    @Nested
    @DisplayName("hold()")
    class HoldTest {

        @Test
        @DisplayName("Stores review comment and keeps status unchanged")
        void hold_StoresReviewCommentAndKeepsStatus() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING, 0);

            // when
            article.hold(99L, "보류 의견을 남깁니다.");

            // then
            assertEquals(99L, article.getApprovedBy());
            assertEquals("보류 의견을 남깁니다.", article.getArticleApprovalOpinion());
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("adminUpdate()")
    class AdminUpdateTest {

        @Test
        @DisplayName("Updates fields without changing status")
        void adminUpdate_UpdatesFieldsWithoutChangingStatus() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.APPROVED, 0);

            // when
            article.adminUpdate(
                    "관리자 수정 제목입니다",
                    ArticleCategory.SAFETY,
                    "관리자 수정 본문입니다. 상태는 유지되고 내용만 변경되는지 확인하기 위한 본문입니다."
            );

            // then
            assertEquals("관리자 수정 제목입니다", article.getArticleTitle());
            assertEquals(ArticleCategory.SAFETY, article.getArticleCategory());
            assertEquals("관리자 수정 본문입니다. 상태는 유지되고 내용만 변경되는지 확인하기 위한 본문입니다.", article.getArticleContent());
            assertEquals(ArticleStatus.APPROVED, article.getArticleStatus());
        }
    }
}
