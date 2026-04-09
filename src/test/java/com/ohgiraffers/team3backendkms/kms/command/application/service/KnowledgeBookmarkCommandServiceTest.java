package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmark;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmarkId;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeBookmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * KnowledgeBookmarkCommandService 단위 테스트
 * - Mockito 사용 — 실제 DB 연동 없음
 * - 테스트 대상: addBookmark(), removeBookmark()
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBookmarkCommandServiceTest {

    @InjectMocks
    private KnowledgeBookmarkCommandService bookmarkCommandService;

    // 실제 repository 대신 Mock 사용
    @Mock
    private KnowledgeBookmarkRepository bookmarkRepository;

    // =====================================================
    // addBookmark() 테스트
    // =====================================================
    @Nested
    @DisplayName("addBookmark()")
    class AddBookmark {

        @Test
        @DisplayName("Saves bookmark successfully")
        // 북마크가 없는 경우 정상 저장되는지 검증
        void addBookmark_Success() {
            // given
            Long articleId = 1L;
            Long employeeId = 5L;
            // 아직 북마크하지 않은 상태
            given(bookmarkRepository.existsById(new KnowledgeBookmarkId(articleId, employeeId))).willReturn(false);

            // when
            bookmarkCommandService.addBookmark(articleId, employeeId);

            // then — save() 호출되었는지 + 저장된 엔티티의 PK 검증
            ArgumentCaptor<KnowledgeBookmark> captor = ArgumentCaptor.forClass(KnowledgeBookmark.class);
            verify(bookmarkRepository).save(captor.capture());
            assertEquals(articleId, captor.getValue().getId().getArticleId());
            assertEquals(employeeId, captor.getValue().getId().getEmployeeId());
        }

        @Test
        @DisplayName("Throws BOOKMARK_001 when already bookmarked")
        // 이미 북마크한 경우 BOOKMARK_001 예외 발생 검증
        void addBookmark_AlreadyExists() {
            // given
            Long articleId = 1L;
            Long employeeId = 5L;
            // 이미 북마크한 상태
            given(bookmarkRepository.existsById(new KnowledgeBookmarkId(articleId, employeeId))).willReturn(true);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookmarkCommandService.addBookmark(articleId, employeeId));
            assertEquals(ArticleErrorCode.BOOKMARK_001, ex.getErrorCode());
            // save()는 호출되면 안 됨
            verify(bookmarkRepository, never()).save(any());
        }
    }

    // =====================================================
    // removeBookmark() 테스트
    // =====================================================
    @Nested
    @DisplayName("removeBookmark()")
    class RemoveBookmark {

        @Test
        @DisplayName("Deletes bookmark successfully")
        // 북마크가 존재하는 경우 정상 삭제되는지 검증
        void removeBookmark_Success() {
            // given
            Long articleId = 1L;
            Long employeeId = 5L;
            KnowledgeBookmarkId id = new KnowledgeBookmarkId(articleId, employeeId);
            // 북마크가 존재하는 상태
            given(bookmarkRepository.existsById(id)).willReturn(true);

            // when
            bookmarkCommandService.removeBookmark(articleId, employeeId);

            // then — deleteById() 호출되었는지 검증
            verify(bookmarkRepository).deleteById(id);
        }

        @Test
        @DisplayName("Throws BOOKMARK_002 when bookmark not found")
        // 북마크가 없는 경우 BOOKMARK_002 예외 발생 검증
        void removeBookmark_NotFound() {
            // given
            Long articleId = 1L;
            Long employeeId = 5L;
            // 북마크가 없는 상태
            given(bookmarkRepository.existsById(new KnowledgeBookmarkId(articleId, employeeId))).willReturn(false);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookmarkCommandService.removeBookmark(articleId, employeeId));
            assertEquals(ArticleErrorCode.BOOKMARK_002, ex.getErrorCode());
            // deleteById()는 호출되면 안 됨
            verify(bookmarkRepository, never()).deleteById(any());
        }
    }
}
