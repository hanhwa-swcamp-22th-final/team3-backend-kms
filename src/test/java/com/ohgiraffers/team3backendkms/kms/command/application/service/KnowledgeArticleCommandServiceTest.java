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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleCommandServiceTest {

    @InjectMocks
    private KnowledgeArticleCommandService knowledgeArticleCommandService;

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
            given(knowledgeArticleRepository.findById(1L))
                    .willReturn(Optional.of(pendingArticle));

            // when
            knowledgeArticleCommandService.incrementViewCount(1L);

            // then
            assertEquals(1, pendingArticle.getViewCount());
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
