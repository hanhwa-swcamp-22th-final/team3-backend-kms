package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
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
    private KnowledgeArticleApprovalService knowledgeArticleApprovalService;

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

    @Nested
    @DisplayName("approve()")
    class ApproveTest {

        @Test
        @DisplayName("Changes status to APPROVED")
        void approve_Success() {
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            knowledgeArticleApprovalService.approve(1L, 99L, "최종 승인합니다.");

            assertEquals(ArticleStatus.APPROVED, pendingArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Throws exception when status is not PENDING (APPROVAL_003)")
        void approve_NotPending_ThrowsException() {
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            assertThrows(BusinessException.class, () ->
                    knowledgeArticleApprovalService.approve(2L, 99L, "잘못된 승인 시도")
            );
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

            knowledgeArticleApprovalService.reject(1L, reviewComment);

            assertEquals(ArticleStatus.REJECTED, pendingArticle.getArticleStatus());
            assertEquals(reviewComment, pendingArticle.getArticleRejectionReason());
        }
    }
}
