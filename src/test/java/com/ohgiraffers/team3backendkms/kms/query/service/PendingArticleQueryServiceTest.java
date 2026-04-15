package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.PendingArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PendingArticleQueryServiceTest {

    private static final long REQUESTER_ID = 20L;

    @InjectMocks
    private PendingArticleQueryService pendingArticleQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Nested
    @DisplayName("getPendingArticleById()")
    class GetPendingArticleById {

        @Test
        @DisplayName("Returns approval article detail from mapper")
        void getPendingArticleById_Success() {
            // given
            PendingArticleDetailDto dto = new PendingArticleDetailDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 상세 조회 제목입니다");

            given(knowledgeArticleMapper.findPendingArticleById(1L, REQUESTER_ID)).willReturn(Optional.of(dto));

            // when
            PendingArticleDetailDto result = pendingArticleQueryService.getPendingArticleById(1L, REQUESTER_ID);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getArticleId());
            assertEquals("승인 상세 조회 제목입니다", result.getArticleTitle());
        }

        @Test
        @DisplayName("Throws exception when article not found (ARTICLE_NOT_FOUND)")
        void getPendingArticleById_NotFound_ThrowsException() {
            // given
            given(knowledgeArticleMapper.findPendingArticleById(anyLong(), anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(ResourceNotFoundException.class, () ->
                pendingArticleQueryService.getPendingArticleById(99L, REQUESTER_ID)
            );
        }
    }

    @Nested
    @DisplayName("getPendingArticles()")
    class GetPendingArticles {

        @Test
        @DisplayName("Returns approval article list from mapper")
        void getPendingArticles_Success() {
            // given
            PendingArticleDto dto = new PendingArticleDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 대기 문서 제목입니다");

            given(knowledgeArticleMapper.findPendingArticles(any())).willReturn(List.of(dto));

            // when
            List<PendingArticleDto> result = pendingArticleQueryService.getPendingArticles(new PendingArticleQueryRequest());

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getArticleId());
        }
    }

    @Nested
    @DisplayName("getPendingStats()")
    class GetPendingStats {

        @Test
        @DisplayName("Returns approval stats from mapper")
        void getPendingStats_Success() {
            // given
            PendingArticleStatsDto dto = new PendingArticleStatsDto();
            dto.setPendingCount(5L);
            dto.setApprovedThisMonth(10L);
            dto.setRejectionRate(33.33);

            given(knowledgeArticleMapper.findPendingStats(REQUESTER_ID)).willReturn(dto);

            // when
            PendingArticleStatsDto result = pendingArticleQueryService.getPendingStats(REQUESTER_ID);

            // then
            assertNotNull(result);
            assertEquals(5L, result.getPendingCount());
            assertEquals(10L, result.getApprovedThisMonth());
            assertEquals(33.33, result.getRejectionRate());
        }
    }
}
