package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
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
    @DisplayName("지식 문서 등록 (register)")
    class RegisterTest {

        @Test
        @DisplayName("정상 등록 시 PENDING 상태로 저장된다")
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

        @Test
        @DisplayName("제목이 5자 미만이면 예외가 발생한다 (ARTICLE_001)")
        void register_TitleTooShort_ThrowsException() {
            // given
            String shortTitle = "짧음";

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    knowledgeArticleService.register(
                            1L, 1L,
                            shortTitle,
                            ArticleCategory.TROUBLESHOOTING,
                            "테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다."
                    )
            );
        }

        @Test
        @DisplayName("본문이 50자 미만이면 예외가 발생한다 (ARTICLE_002)")
        void register_ContentTooShort_ThrowsException() {
            // given
            String shortContent = "짧은 본문";

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    knowledgeArticleService.register(
                            1L, 1L,
                            "테스트 지식 문서 제목입니다",
                            ArticleCategory.TROUBLESHOOTING,
                            shortContent
                    )
            );
        }

        @Test
        @DisplayName("본문이 10,000자를 넘으면 예외가 발생한다 (ARTICLE_003)")
        void register_ContentTooLong_ThrowsException() {
            // given
            String longContent = "a".repeat(10001);

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    knowledgeArticleService.register(
                            1L, 1L,
                            "테스트 지식 문서 제목입니다",
                            ArticleCategory.TROUBLESHOOTING,
                            longContent
                    )
            );
        }
    }

    // =========================================================
    // draft()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 임시저장 (draft)")
    class DraftTest {

        @Test
        @DisplayName("임시저장 시 DRAFT 상태로 저장된다")
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
    // getDetail()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 상세 조회 (getDetail)")
    class GetDetailTest {

        @Test
        @DisplayName("정상 조회 시 조회수가 1 증가한다")
        void getDetail_Success() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleService.getDetail(1L);

            // then
            assertEquals(1, pendingArticle.getViewCount());
        }

        @Test
        @DisplayName("삭제된 문서를 조회하면 예외가 발생한다 (ARTICLE_008)")
        void getDetail_DeletedArticle_ThrowsException() {
            // given
            KnowledgeArticle deletedArticle = KnowledgeArticle.builder()
                    .articleId(3L)
                    .authorId(1L)
                    .articleStatus(ArticleStatus.DRAFT)
                    .isDeleted(true)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(deletedArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.getDetail(3L)
            );
        }
    }

    // =========================================================
    // approve()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 승인 (approve)")
    class ApproveTest {

        @Test
        @DisplayName("PENDING 문서를 승인하면 APPROVED 상태로 바뀐다")
        void approve_Success() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleService.approve(1L, 99L, "잘 작성된 문서입니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, pendingArticle.getArticleStatus());
        }

        @Test
        @DisplayName("PENDING이 아닌 문서를 승인하면 예외가 발생한다 (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            // given
            KnowledgeArticle approvedArticle = KnowledgeArticle.builder()
                    .articleId(4L)
                    .authorId(1L)
                    .articleStatus(ArticleStatus.APPROVED)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(4L))
                    .willReturn(Optional.of(approvedArticle));

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    knowledgeArticleService.approve(4L, 99L, "재승인 시도")
            );
        }
    }

    // =========================================================
    // reject()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 반려 (reject)")
    class RejectTest {

        @Test
        @DisplayName("PENDING 문서를 반려하면 REJECTED 상태로 바뀌고 반려 사유가 저장된다")
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
    @DisplayName("지식 문서 삭제 (delete)")
    class DeleteTest {

        @Test
        @DisplayName("본인 문서를 삭제하면 is_deleted가 true로 바뀐다")
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
        @DisplayName("타인의 문서를 삭제하면 예외가 발생한다 (ARTICLE_007)")
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
        @DisplayName("승인 완료된 문서를 삭제하면 예외가 발생한다 (ARTICLE_009)")
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
}
