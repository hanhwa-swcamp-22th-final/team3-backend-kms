package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleServiceTest {

    @InjectMocks
    private KnowledgeArticleService knowledgeArticleService;

    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Mock
    private IdGenerator idGenerator;

    private KnowledgeArticle pendingArticle;
    private KnowledgeArticle draftArticle;

    @BeforeEach
    void setUp() {
        pendingArticle = KnowledgeArticle.builder()
                .articleId(1L)
                .authorId(1L)
                .articleTitle("테스트 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(false)
                .viewCount(0)
                .build();

        draftArticle = KnowledgeArticle.builder()
                .articleId(2L)
                .authorId(1L)
                .articleTitle("임시저장 문서 제목입니다")
                .articleCategory(ArticleCategory.PROCESS_IMPROVEMENT)
                .articleContent("임시저장 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    // =========================================================
    // register()
    // =========================================================

    @Nested
    // 지식 문서 등록 (register)
    @DisplayName("register()")
    class RegisterTest {

        @Test
        // 정상 등록 시 PENDING 상태로 저장된다
        @DisplayName("Saves article with PENDING status")
        void register_Success() {
            // given
            given(knowledgeArticleRepository.save(any(KnowledgeArticle.class)))
                    .willReturn(pendingArticle);

            // when
            knowledgeArticleService.register(
                    1L, 1L,
                    "테스트 지식 문서 제목입니다",
                    ArticleCategory.TROUBLESHOOTING,
                    "테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 추가로 작성한 내용입니다."
            );

            // then
            ArgumentCaptor<KnowledgeArticle> captor = ArgumentCaptor.forClass(KnowledgeArticle.class);
            verify(knowledgeArticleRepository).save(captor.capture());
            assertEquals(ArticleStatus.PENDING, captor.getValue().getArticleStatus());
        }

    }

    // =========================================================
    // draft()
    // =========================================================

    @Nested
    // 지식 문서 임시저장 (draft)
    @DisplayName("draft()")
    class DraftTest {

        @Test
        // 임시저장 시 DRAFT 상태로 저장된다
        @DisplayName("Saves article with DRAFT status")
        void draft_Success() {
            // given
            given(knowledgeArticleRepository.save(any(KnowledgeArticle.class)))
                    .willReturn(draftArticle);

            // when
            knowledgeArticleService.draft(
                    1L, 1L,
                    "임시저장 문서 제목입니다",
                    ArticleCategory.PROCESS_IMPROVEMENT,
                    "임시저장 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 추가로 작성한 내용입니다."
            );

            // then
            ArgumentCaptor<KnowledgeArticle> captor = ArgumentCaptor.forClass(KnowledgeArticle.class);
            verify(knowledgeArticleRepository).save(captor.capture());
            assertEquals(ArticleStatus.DRAFT, captor.getValue().getArticleStatus());
        }
    }

    // =========================================================
    // incrementViewCount()
    // =========================================================

    @Nested
    // 조회수 증가 (incrementViewCount)
    @DisplayName("incrementViewCount()")
    class IncrementViewCountTest {

        @Test
        // 조회수가 1 증가한다
        @DisplayName("Increments view count by 1")
        void incrementViewCount_Success() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleService.incrementViewCount(1L);

            // then
            assertEquals(1, pendingArticle.getViewCount());
        }
    }

    // =========================================================
    // approve()
    // =========================================================

    @Nested
    // 승인 (approve) — TL 또는 DL
    @DisplayName("approve()")
    class ApproveTest {

        @Test
        // PENDING 문서를 승인하면 APPROVED 상태로 바뀐다
        @DisplayName("Changes status to APPROVED")
        void approve_Success() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleService.approve(1L, 99L, "최종 승인합니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, pendingArticle.getArticleStatus());
        }

        @Test
        // PENDING이 아닌 문서를 승인하면 예외가 발생한다 (APPROVAL_003)
        @DisplayName("Throws exception when status is not PENDING (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.approve(2L, 99L, "잘못된 승인 시도")
            );
        }
    }

    // =========================================================
    // reject()
    // =========================================================

    @Nested
    // 지식 문서 반려 (reject)
    @DisplayName("reject()")
    class RejectTest {

        @Test
        // PENDING 문서를 반려하면 REJECTED 상태로 바뀌고 반려 사유가 저장된다
        @DisplayName("Changes status to REJECTED and saves rejection reason")
        void reject_Success() {
            // given
            String reason = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";

            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleService.reject(1L, reason);

            // then
            assertEquals(ArticleStatus.REJECTED, pendingArticle.getArticleStatus());
            assertEquals(reason, pendingArticle.getArticleRejectionReason());
        }
    }

    // =========================================================
    // delete()
    // =========================================================

    @Nested
    // 지식 문서 삭제 (delete)
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        // 본인 문서를 삭제하면 is_deleted가 true로 바뀐다
        @DisplayName("Sets isDeleted to true")
        void delete_Success() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleService.delete(2L, 1L);

            // then
            assertTrue(draftArticle.getIsDeleted());
        }

        @Test
        // 타인의 문서를 삭제하면 예외가 발생한다 (ARTICLE_007)
        @DisplayName("Throws exception when requester is not the author (ARTICLE_007)")
        void delete_NotAuthor_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.delete(2L, 999L)  // 다른 사람 ID
            );
        }

        @Test
        // 승인 완료된 문서를 삭제하면 예외가 발생한다 (ARTICLE_009)
        @DisplayName("Throws exception when status is APPROVED (ARTICLE_009)")
        void delete_ApprovedArticle_ThrowsException() {
            // given
            KnowledgeArticle approvedArticle = KnowledgeArticle.builder()
                    .articleId(5L)
                    .authorId(1L)
                    .articleStatus(ArticleStatus.APPROVED)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(5L))
                    .willReturn(Optional.of(approvedArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.delete(5L, 1L)
            );
        }
    }

    // =========================================================
    // update()
    // =========================================================

    @Nested
    // 지식 문서 수정 (update) — DRAFT 상태에서만 가능, 수정 후 PENDING 전환
    @DisplayName("update()")
    class UpdateTest {

        @Test
        // DRAFT 문서를 수정하면 PENDING 상태로 바뀐다
        @DisplayName("Updates article and changes status to PENDING when DRAFT")
        void update_Success() {
            // given
            String newTitle = "수정된 지식 문서 제목입니다";
            ArticleCategory newCategory = ArticleCategory.PROCESS_IMPROVEMENT;
            String newContent = "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 추가로 작성한 내용입니다.";

            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleService.update(2L, newTitle, newCategory, newContent, 1L);

            // then
            assertEquals(newTitle, draftArticle.getArticleTitle());
            assertEquals(newCategory, draftArticle.getArticleCategory());
            assertEquals(newContent, draftArticle.getArticleContent());
            assertEquals(ArticleStatus.PENDING, draftArticle.getArticleStatus());
        }

        @Test
        // 타인의 문서를 수정하면 예외가 발생한다 (ARTICLE_007)
        @DisplayName("Throws exception when requester is not the author (ARTICLE_007)")
        void update_NotAuthor_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.update(2L, "수정된 제목", ArticleCategory.TROUBLESHOOTING,
                            "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.", 999L)
            );
        }

    }

    // =========================================================
    // adminDelete()
    // =========================================================

    @Nested
    // 관리자 삭제 (adminDelete) — Admin만 사용, 모든 상태 삭제 가능
    @DisplayName("adminDelete()")
    class AdminDeleteTest {

        @Test
        // 문서를 관리자가 삭제 사유와 함께 삭제하면 isDeleted=true, deletionReason이 저장된다
        @DisplayName("Sets isDeleted to true and saves deletion reason")
        void adminDelete_Success() {
            // given
            KnowledgeArticle approvedArticle = KnowledgeArticle.builder()
                    .articleId(5L)
                    .authorId(1L)
                    .articleStatus(ArticleStatus.APPROVED)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();
            String deletionReason = "지식 문서 정책 위반으로 인한 삭제입니다. 해당 문서는 더 이상 참고할 수 없습니다.";

            given(knowledgeArticleRepository.findById(5L))
                    .willReturn(Optional.of(approvedArticle));

            // when
            knowledgeArticleService.adminDelete(5L, deletionReason);

            // then
            assertTrue(approvedArticle.getIsDeleted());
            assertEquals(deletionReason, approvedArticle.getArticleDeletionReason());
        }

        @Test
        // 삭제 사유가 10자 미만이면 예외가 발생한다 (ARTICLE_012)
        @DisplayName("Throws exception when reason is less than 10 characters (ARTICLE_012)")
        void adminDelete_ReasonTooShort_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    knowledgeArticleService.adminDelete(1L, "짧음")
            );
        }

        @Test
        // 삭제 사유가 500자를 넘으면 예외가 발생한다 (ARTICLE_012)
        @DisplayName("Throws exception when reason exceeds 500 characters (ARTICLE_012)")
        void adminDelete_ReasonTooLong_ThrowsException() {
            // given
            String longReason = "a".repeat(501);
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    knowledgeArticleService.adminDelete(1L, longReason)
            );
        }
    }
}
