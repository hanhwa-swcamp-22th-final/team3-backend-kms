package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleApprovalServiceTest {

    @InjectMocks
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Mock
    private IdGenerator idGenerator;

    private KnowledgeArticle pendingArticle;
    private KnowledgeArticle draftArticle;
    private KnowledgeArticle approvedArticle;
    private KnowledgeArticle rejectedArticle;
    private KnowledgeArticle deletedArticle;

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

        approvedArticle = KnowledgeArticle.builder()
                .articleId(3L)
                .authorId(1L)
                .articleStatus(ArticleStatus.APPROVED)
                .isDeleted(false)
                .viewCount(0)
                .build();

        rejectedArticle = KnowledgeArticle.builder()
                .articleId(4L)
                .authorId(1L)
                .articleStatus(ArticleStatus.REJECTED)
                .isDeleted(false)
                .viewCount(0)
                .build();

        deletedArticle = KnowledgeArticle.builder()
                .articleId(5L)
                .authorId(1L)
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(true)
                .viewCount(0)
                .build();
    }

    @Nested
    @DisplayName("approve()")
    class ApproveTest {

        @Test
        @DisplayName("Changes status to APPROVED")
        void approve_Success() {
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            knowledgeArticleCommandService.approve(1L, 99L, "최종 승인합니다.");

            assertEquals(ArticleStatus.APPROVED, pendingArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Throws exception when status is not PENDING (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.approve(2L, 99L, "잘못된 승인 시도")
            );

            assertEquals(ArticleErrorCode.APPROVAL_003, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is already APPROVED (APPROVAL_005)")
        void approve_AlreadyApproved_ThrowsException() {
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.approve(3L, 99L, "재승인 시도")
            );

            assertEquals(ArticleErrorCode.APPROVAL_005, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is REJECTED (APPROVAL_006)")
        void approve_RejectedArticle_ThrowsException() {
            given(knowledgeArticleRepository.findById(4L))
                    .willReturn(Optional.of(rejectedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.approve(4L, 99L, "반려 문서 승인 시도")
            );

            assertEquals(ArticleErrorCode.APPROVAL_006, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is deleted (ARTICLE_008)")
        void approve_DeletedArticle_ThrowsException() {
            given(knowledgeArticleRepository.findById(5L))
                    .willReturn(Optional.of(deletedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.approve(5L, 99L, "삭제된 문서 승인 시도")
            );

            assertEquals(ArticleErrorCode.ARTICLE_008, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTest {

        @Test
        @DisplayName("Changes status to REJECTED and saves review comment")
        void reject_Success() {
            String reviewComment = "내용이 충분하지 않습니다. 보완 후 재제출해주세요.";

            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            knowledgeArticleCommandService.reject(1L, reviewComment);

            assertEquals(ArticleStatus.REJECTED, pendingArticle.getArticleStatus());
            assertEquals(reviewComment, pendingArticle.getArticleRejectionReason());
        }

        @Test
        @DisplayName("Throws exception when status is not PENDING (APPROVAL_003)")
        void reject_NotPending_ThrowsException() {
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.reject(2L, "잘못된 반려 시도입니다. 10자 이상.")
            );

            assertEquals(ArticleErrorCode.APPROVAL_003, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is already REJECTED (APPROVAL_007)")
        void reject_AlreadyRejected_ThrowsException() {
            given(knowledgeArticleRepository.findById(4L))
                    .willReturn(Optional.of(rejectedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.reject(4L, "이미 반려된 문서 재반려 시도.")
            );

            assertEquals(ArticleErrorCode.APPROVAL_007, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is APPROVED (APPROVAL_008)")
        void reject_ApprovedArticle_ThrowsException() {
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.reject(3L, "승인 완료 문서 반려 시도입니다.")
            );

            assertEquals(ArticleErrorCode.APPROVAL_008, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is deleted (ARTICLE_008)")
        void reject_DeletedArticle_ThrowsException() {
            given(knowledgeArticleRepository.findById(5L))
                    .willReturn(Optional.of(deletedArticle));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.reject(5L, "삭제된 문서 반려 시도입니다.")
            );

            assertEquals(ArticleErrorCode.ARTICLE_008, exception.getErrorCode());
        }
    }
}
