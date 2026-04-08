package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeEditHistoryRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleCommandServiceTest {

    @InjectMocks
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Mock
    private KnowledgeEditHistoryRepository knowledgeEditHistoryRepository;

    @Mock
    private IdGenerator idGenerator;

    private KnowledgeArticle pendingArticle;
    private KnowledgeArticle draftArticle;
    private KnowledgeArticle approvedArticle;

    @BeforeEach
    void setUp() {
        pendingArticle = KnowledgeArticle.builder()
                .articleId(1L)
                .authorId(1L)
                .articleTitle("테스트 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(ArticleStatus.PENDING)
                .approvalVersion(0)
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
                .approvalVersion(0)
                .isDeleted(false)
                .viewCount(0)
                .build();

        approvedArticle = KnowledgeArticle.builder()
                .articleId(3L)
                .authorId(1L)
                .articleTitle("승인된 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("승인된 문서 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(ArticleStatus.APPROVED)
                .approvalVersion(1)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    @Nested
    @DisplayName("register()")
    class RegisterTest {

        @Test
        @DisplayName("Saves article with PENDING status")
        void register_Success() {
            // given
            given(knowledgeArticleRepository.save(any(KnowledgeArticle.class)))
                    .willReturn(pendingArticle);

            // when
            knowledgeArticleCommandService.register(
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

    @Nested
    @DisplayName("draft()")
    class DraftTest {

        @Test
        @DisplayName("Saves article with DRAFT status")
        void draft_Success() {
            // given
            given(knowledgeArticleRepository.save(any(KnowledgeArticle.class)))
                    .willReturn(draftArticle);

            // when
            knowledgeArticleCommandService.draft(
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

    @Nested
    @DisplayName("incrementViewCount()")
    class IncrementViewCountTest {

        @Test
        @DisplayName("Increments view count by 1")
        void incrementViewCount_Success() {
            // given
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));

            // when
            knowledgeArticleCommandService.incrementViewCount(3L);

            // then
            assertEquals(1, approvedArticle.getViewCount());
        }

        @Test
        @DisplayName("Does not increment view count for DRAFT article")
        void incrementViewCount_DraftArticle_NoIncrement() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleCommandService.incrementViewCount(2L);

            // then
            assertEquals(0, draftArticle.getViewCount());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("Sets isDeleted to true")
        void delete_Success() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleCommandService.delete(2L, 1L);

            // then
            assertTrue(draftArticle.getIsDeleted());
        }

        @Test
        @DisplayName("Throws exception when requester is not the author (ARTICLE_007)")
        void delete_NotAuthor_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.delete(2L, 999L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_007, exception.getErrorCode());
        }

        @Test
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
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.delete(5L, 1L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_009, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when status is PENDING (ARTICLE_010)")
        void delete_PendingArticle_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.delete(1L, 1L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_010, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("updateDraft()")
    class UpdateTest {

        @Test
        @DisplayName("Updates article and keeps status as DRAFT when DRAFT")
        void updateDraft_Success() {
            // given
            String newTitle = "수정된 지식 문서 제목입니다";
            ArticleCategory newCategory = ArticleCategory.PROCESS_IMPROVEMENT;
            Long newEquipmentId = 99L;
            String newContent = "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다. 추가로 작성한 내용입니다.";

            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleCommandService.updateDraft(2L, newTitle, newCategory, newEquipmentId, newContent, 1L);

            // then
            assertEquals(newTitle, draftArticle.getArticleTitle());
            assertEquals(newCategory, draftArticle.getArticleCategory());
            assertEquals(newEquipmentId, draftArticle.getEquipmentId());
            assertEquals(newContent, draftArticle.getArticleContent());
            assertEquals(ArticleStatus.DRAFT, draftArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Updates article and keeps status as PENDING when PENDING")
        void updateDraft_PendingArticle_Success() {
            // given
            String newTitle = "수정된 승인대기 문서 제목입니다";
            ArticleCategory newCategory = ArticleCategory.PROCESS_IMPROVEMENT;
            Long newEquipmentId = 77L;
            String newContent = "승인대기 상태에서 수정된 본문 내용입니다. 승인 전 단계에서는 수정이 가능해야 함을 검증합니다.";

            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleCommandService.updateDraft(1L, newTitle, newCategory, newEquipmentId, newContent, 1L);

            // then
            assertEquals(newTitle, pendingArticle.getArticleTitle());
            assertEquals(newCategory, pendingArticle.getArticleCategory());
            assertEquals(newEquipmentId, pendingArticle.getEquipmentId());
            assertEquals(newContent, pendingArticle.getArticleContent());
            assertEquals(ArticleStatus.PENDING, pendingArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Throws exception when requester is not the author (ARTICLE_007)")
        void updateDraft_NotAuthor_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.updateDraft(
                            2L,
                            "수정된 제목",
                            ArticleCategory.TROUBLESHOOTING,
                            1L,
                            "수정된 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.",
                            999L
                    )
            );

            assertEquals(ArticleErrorCode.ARTICLE_007, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("submitDraft()")
    class SubmitDraftTest {

        @Test
        @DisplayName("Updates article and changes status to PENDING when DRAFT")
        void submitDraft_Success() {
            // given
            String newTitle = "제출할 지식 문서 제목입니다";
            ArticleCategory newCategory = ArticleCategory.PROCESS_IMPROVEMENT;
            Long newEquipmentId = 99L;
            String newContent = "제출용 본문 내용입니다. 최소 50자 이상이어야 하며 제출 시 상태 변경까지 함께 검증합니다.";

            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when
            knowledgeArticleCommandService.submitDraft(2L, newTitle, newCategory, newEquipmentId, newContent, 1L);

            // then
            assertEquals(newTitle, draftArticle.getArticleTitle());
            assertEquals(newCategory, draftArticle.getArticleCategory());
            assertEquals(newEquipmentId, draftArticle.getEquipmentId());
            assertEquals(newContent, draftArticle.getArticleContent());
            assertEquals(ArticleStatus.PENDING, draftArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Updates article and keeps status as PENDING when PENDING")
        void submitDraft_PendingArticle_Success() {
            // given
            String newTitle = "승인대기 중 다시 수정한 제목입니다";
            ArticleCategory newCategory = ArticleCategory.PROCESS_IMPROVEMENT;
            Long newEquipmentId = 55L;
            String newContent = "이미 승인대기 중인 문서의 본문을 수정한 내용입니다. 수정 후에도 상태는 승인대기로 유지됩니다.";

            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleCommandService.submitDraft(1L, newTitle, newCategory, newEquipmentId, newContent, 1L);

            // then
            assertEquals(newTitle, pendingArticle.getArticleTitle());
            assertEquals(newCategory, pendingArticle.getArticleCategory());
            assertEquals(newEquipmentId, pendingArticle.getEquipmentId());
            assertEquals(newContent, pendingArticle.getArticleContent());
            assertEquals(ArticleStatus.PENDING, pendingArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Changes REJECTED article to PENDING after update")
        void submitDraft_RejectedArticle_Success() {
            // given
            KnowledgeArticle rejectedArticle = KnowledgeArticle.builder()
                    .articleId(4L)
                    .authorId(1L)
                    .articleTitle("반려된 문서 제목입니다")
                    .articleCategory(ArticleCategory.TROUBLESHOOTING)
                    .articleContent("반려된 문서 본문 내용입니다. 수정 후 다시 제출하는 흐름을 검증하기 위한 본문입니다.")
                    .articleStatus(ArticleStatus.REJECTED)
                    .approvalVersion(1)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(4L))
                    .willReturn(Optional.of(rejectedArticle));

            // when
            knowledgeArticleCommandService.submitDraft(
                    4L,
                    "재제출 제목입니다",
                    ArticleCategory.PROCESS_IMPROVEMENT,
                    99L,
                    "반려 후 재제출하는 본문 내용입니다. 충분한 길이의 내용을 담아 다시 제출하는 흐름을 검증합니다.",
                    1L
            );

            // then
            assertEquals(ArticleStatus.PENDING, rejectedArticle.getArticleStatus());
        }
    }

    @Nested
    @DisplayName("startRevision()")
    class StartRevisionTest {

        @Test
        @DisplayName("Stores history once and changes APPROVED article to DRAFT")
        void startRevision_Success() {
            // given
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));
            given(knowledgeEditHistoryRepository.existsByArticleIdAndApprovalVersion(3L, 1))
                    .willReturn(false);
            given(idGenerator.generate()).willReturn(100L);

            // when
            knowledgeArticleCommandService.startRevision(3L, 1L);

            // then
            verify(knowledgeEditHistoryRepository).save(any());
            assertEquals(ArticleStatus.DRAFT, approvedArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Does not store duplicate history for same approval version")
        void startRevision_WhenHistoryExists_DoesNotSaveHistory() {
            // given
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));
            given(knowledgeEditHistoryRepository.existsByArticleIdAndApprovalVersion(3L, 1))
                    .willReturn(true);

            // when
            knowledgeArticleCommandService.startRevision(3L, 1L);

            // then
            verify(knowledgeEditHistoryRepository).existsByArticleIdAndApprovalVersion(3L, 1);
            verify(knowledgeEditHistoryRepository, never()).save(any());
            assertEquals(ArticleStatus.DRAFT, approvedArticle.getArticleStatus());
        }

        @Test
        @DisplayName("Throws exception when article is not APPROVED")
        void startRevision_WhenNotApproved_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(2L))
                    .willReturn(Optional.of(draftArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.startRevision(2L, 1L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_011, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when article is already deleted")
        void startRevision_WhenDeleted_ThrowsException() {
            // given
            KnowledgeArticle deletedApprovedArticle = KnowledgeArticle.builder()
                    .articleId(7L)
                    .authorId(1L)
                    .articleTitle("삭제된 승인 문서입니다")
                    .articleCategory(ArticleCategory.TROUBLESHOOTING)
                    .articleContent("삭제된 승인 문서 본문 내용입니다. 수정 시작 전 삭제 여부를 검증하기 위한 본문입니다.")
                    .articleStatus(ArticleStatus.APPROVED)
                    .approvalVersion(1)
                    .isDeleted(true)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(7L))
                    .willReturn(Optional.of(deletedApprovedArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.startRevision(7L, 1L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_008, exception.getErrorCode());
        }

        @Test
        @DisplayName("Throws exception when requester is not the author")
        void startRevision_WhenRequesterIsNotAuthor_ThrowsException() {
            // given
            given(knowledgeArticleRepository.findById(3L))
                    .willReturn(Optional.of(approvedArticle));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    knowledgeArticleCommandService.startRevision(3L, 999L)
            );

            assertEquals(ArticleErrorCode.ARTICLE_007, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("approve()")
    class ApproveCommandTest {

        @Test
        @DisplayName("Increases approval version on approval")
        void approve_IncreasesApprovalVersion() {
            // given
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleCommandService.approve(1L, 20L, "승인합니다.");

            // then
            assertEquals(ArticleStatus.APPROVED, pendingArticle.getArticleStatus());
            assertEquals(1, pendingArticle.getApprovalVersion());
        }

        @Test
        @DisplayName("Increases approval version again on re-approval")
        void approve_Reapproval_IncreasesApprovalVersionAgain() {
            // given
            KnowledgeArticle pendingRevisionArticle = KnowledgeArticle.builder()
                    .articleId(6L)
                    .authorId(1L)
                    .articleTitle("재승인 대기 문서입니다")
                    .articleCategory(ArticleCategory.TROUBLESHOOTING)
                    .articleContent("재승인 전 본문 내용입니다. 다시 승인될 때 버전이 증가하는지를 확인합니다.")
                    .articleStatus(ArticleStatus.PENDING)
                    .approvalVersion(2)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(6L))
                    .willReturn(Optional.of(pendingRevisionArticle));

            // when
            knowledgeArticleCommandService.approve(6L, 20L, "재승인합니다.");

            // then
            assertEquals(3, pendingRevisionArticle.getApprovalVersion());
        }
    }

    @Nested
    @DisplayName("approved article rejected flow")
    class ApprovedRejectedFlowTest {

        @Test
        @DisplayName("Keeps one history snapshot and allows resubmit after rejection")
        void approvedRevisionRejectedFlow_Success() {
            // given
            KnowledgeArticle article = KnowledgeArticle.builder()
                    .articleId(8L)
                    .authorId(1L)
                    .articleTitle("승인된 원본 제목입니다")
                    .articleCategory(ArticleCategory.TROUBLESHOOTING)
                    .articleContent("승인된 원본 본문입니다. 수정 시작 전에 스냅샷으로 저장되어야 하는 승인본 내용입니다.")
                    .articleStatus(ArticleStatus.APPROVED)
                    .approvalVersion(1)
                    .isDeleted(false)
                    .viewCount(0)
                    .build();

            given(knowledgeArticleRepository.findById(8L))
                    .willReturn(Optional.of(article));
            given(knowledgeEditHistoryRepository.existsByArticleIdAndApprovalVersion(8L, 1))
                    .willReturn(false);
            given(idGenerator.generate()).willReturn(101L);

            // when
            knowledgeArticleCommandService.startRevision(8L, 1L);
            knowledgeArticleCommandService.submitDraft(
                    8L,
                    "재승인 요청 제목입니다",
                    ArticleCategory.PROCESS_IMPROVEMENT,
                    88L,
                    "재승인 요청 본문입니다. 수정 후 승인 대기 상태로 보내는 단계입니다. 충분한 길이를 확보했습니다.",
                    1L
            );
            knowledgeArticleCommandService.reject(8L, "반려 사유를 충분한 길이로 남깁니다.");
            knowledgeArticleCommandService.updateDraft(
                    8L,
                    "반려 후 수정 제목입니다",
                    ArticleCategory.SAFETY,
                    99L,
                    "반려 후 다시 수정한 본문입니다. 수정 이력은 이미 저장되어 있고 다시 제출 가능해야 합니다.",
                    1L
            );
            knowledgeArticleCommandService.submitDraft(
                    8L,
                    "반려 후 재제출 제목입니다",
                    ArticleCategory.SAFETY,
                    99L,
                    "반려 후 재제출하는 본문입니다. 최종적으로 다시 승인 대기로 전환되는지를 검증합니다.",
                    1L
            );

            // then
            verify(knowledgeEditHistoryRepository, times(1)).save(any());
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
            assertEquals(1, article.getApprovalVersion());
            assertEquals("반려 후 재제출 제목입니다", article.getArticleTitle());
        }
    }

    @Nested
    @DisplayName("adminDelete()")
    class AdminDeleteTest {

        @Test
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
            knowledgeArticleCommandService.adminDelete(5L, deletionReason);

            // then
            assertTrue(approvedArticle.getIsDeleted());
            assertEquals(deletionReason, approvedArticle.getArticleDeletionReason());
        }

    }
}
