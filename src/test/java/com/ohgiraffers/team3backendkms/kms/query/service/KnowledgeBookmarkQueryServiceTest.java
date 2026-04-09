package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeBookmarkMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

/**
 * KnowledgeBookmarkQueryService 단위 테스트
 * - Mockito 사용 — 실제 DB 연동 없음
 * - 테스트 대상: getMyBookmarks()
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBookmarkQueryServiceTest {

    @InjectMocks
    private KnowledgeBookmarkQueryService bookmarkQueryService;

    // 실제 mapper 대신 Mock 사용
    @Mock
    private KnowledgeBookmarkMapper bookmarkMapper;

    // =====================================================
    // getMyBookmarks() 테스트
    // =====================================================
    @Nested
    @DisplayName("getMyBookmarks()")
    class GetMyBookmarks {

        @Test
        @DisplayName("Returns bookmark list")
        // 북마크 목록이 있는 경우 정상 반환되는지 검증
        void getMyBookmarks_Success() {
            // given
            Long employeeId = 5L;
            // 반환될 북마크 게시글 데이터 세팅
            ArticleReadDto dto = new ArticleReadDto();
            dto.setArticleId(1L);
            dto.setAuthorId(2L);
            dto.setAuthorName("홍길동");
            dto.setArticleTitle("테스트 게시글 제목입니다");
            dto.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto.setArticleStatus(ArticleStatus.APPROVED);
            dto.setViewCount(10);
            dto.setCreatedAt(LocalDateTime.now());
            given(bookmarkMapper.findBookmarksByEmployeeId(employeeId)).willReturn(List.of(dto));

            // when
            List<ArticleReadDto> result = bookmarkQueryService.getMyBookmarks(employeeId);

            // then — 반환된 목록 크기 및 데이터 검증
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getArticleId());
            assertEquals("홍길동", result.get(0).getAuthorName());
        }

        @Test
        @DisplayName("Returns empty list when no bookmarks")
        // 북마크가 없는 경우 빈 목록 반환되는지 검증
        void getMyBookmarks_EmptyList() {
            // given
            Long employeeId = 5L;
            // 북마크가 없는 상태
            given(bookmarkMapper.findBookmarksByEmployeeId(employeeId)).willReturn(List.of());

            // when
            List<ArticleReadDto> result = bookmarkQueryService.getMyBookmarks(employeeId);

            // then
            assertTrue(result.isEmpty());
        }
    }
}
