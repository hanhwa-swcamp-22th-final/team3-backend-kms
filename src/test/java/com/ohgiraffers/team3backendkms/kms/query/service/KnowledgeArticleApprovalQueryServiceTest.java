package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
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
class KnowledgeArticleApprovalQueryServiceTest {

    @InjectMocks
    private KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Nested
    @DisplayName("getApprovalArticleById()")
    class GetApprovalArticleById {

        @Test
        @DisplayName("Returns approval article detail from mapper")
        void getApprovalArticleById_Success() {
            // given
            ApprovalArticleDetailDto dto = new ApprovalArticleDetailDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 상세 조회 제목입니다");

            given(knowledgeArticleMapper.findApprovalArticleById(1L)).willReturn(Optional.of(dto));

            // when
            ApprovalArticleDetailDto result = knowledgeArticleApprovalQueryService.getApprovalArticleById(1L);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getArticleId());
            assertEquals("승인 상세 조회 제목입니다", result.getArticleTitle());
        }

        @Test
        @DisplayName("Throws exception when article not found (ARTICLE_NOT_FOUND)")
        void getApprovalArticleById_NotFound_ThrowsException() {
            // given
            given(knowledgeArticleMapper.findApprovalArticleById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThrows(ResourceNotFoundException.class, () ->
                knowledgeArticleApprovalQueryService.getApprovalArticleById(99L)
            );
        }
    }

    @Nested
    @DisplayName("getApprovalArticles()")
    class GetApprovalArticles {

        @Test
        @DisplayName("Returns approval article list from mapper")
        void getApprovalArticles_Success() {
            // given
            ApprovalArticleDto dto = new ApprovalArticleDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 대기 문서 제목입니다");

            given(knowledgeArticleMapper.findApprovalArticles(any())).willReturn(List.of(dto));

            // when
            List<ApprovalArticleDto> result = knowledgeArticleApprovalQueryService.getApprovalArticles(new ApprovalQueryRequest());

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getArticleId());
        }
    }

    @Nested
    @DisplayName("getApprovalStats()")
    class GetApprovalStats {

        @Test
        @DisplayName("Returns approval stats from mapper")
        void getApprovalStats_Success() {
            // given
            ApprovalStatsDto dto = new ApprovalStatsDto();
            dto.setPendingCount(5L);
            dto.setApprovedThisMonth(10L);
            dto.setRejectionRate(33.33);

            given(knowledgeArticleMapper.findApprovalStats()).willReturn(dto);

            // when
            ApprovalStatsDto result = knowledgeArticleApprovalQueryService.getApprovalStats();

            // then
            assertNotNull(result);
            assertEquals(5L, result.getPendingCount());
            assertEquals(10L, result.getApprovedThisMonth());
            assertEquals(33.33, result.getRejectionRate());
        }
    }
}
