package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeTagMapper;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleMyQueryServiceTest {

    @InjectMocks
    private KnowledgeArticleMyQueryService knowledgeArticleMyQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Mock
    private KnowledgeTagMapper knowledgeTagMapper;

    @Nested
    @DisplayName("getMyArticleStats()")
    class GetMyArticleStats {

        @Test
        @DisplayName("Returns stats DTO")
        void getMyArticleStats_success() {
            MyArticleStatsDto dto = new MyArticleStatsDto();
            dto.setApprovedCount(3L);
            dto.setPendingCount(1L);
            dto.setRejectedCount(2L);
            dto.setDraftCount(4L);
            given(knowledgeArticleMapper.findMyArticleStats(10L)).willReturn(dto);

            MyArticleStatsDto result = knowledgeArticleMyQueryService.getMyArticleStats(10L);

            assertNotNull(result);
            assertEquals(3L, result.getApprovedCount());
            assertEquals(4L, result.getDraftCount());
        }
    }

    @Nested
    @DisplayName("getMyArticles()")
    class GetMyArticles {

        @Test
        @DisplayName("Returns article list with tags")
        void getMyArticles_success() {
            MyArticleQueryRequest request = new MyArticleQueryRequest();

            MyArticleDto article = new MyArticleDto();
            article.setArticleId(1L);
            article.setArticleTitle("내 문서");
            article.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            article.setArticleStatus(ArticleStatus.APPROVED);
            article.setCreatedAt(LocalDateTime.now());

            KnowledgeTagReadDto tag = new KnowledgeTagReadDto();
            tag.setTagId(100L);
            tag.setTagName("가공");

            given(knowledgeArticleMapper.findMyArticles(10L, request)).willReturn(List.of(article));
            given(knowledgeTagMapper.findTagsByArticleId(1L)).willReturn(List.of(tag));

            List<MyArticleDto> result = knowledgeArticleMyQueryService.getMyArticles(10L, request);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("내 문서", result.get(0).getArticleTitle());
            assertEquals(1, result.get(0).getTags().size());
            assertEquals("가공", result.get(0).getTags().get(0).getTagName());
        }

        @Test
        @DisplayName("Returns empty list when no article exists")
        void getMyArticles_whenNoData_thenEmptyList() {
            MyArticleQueryRequest request = new MyArticleQueryRequest();
            given(knowledgeArticleMapper.findMyArticles(10L, request)).willReturn(List.of());

            List<MyArticleDto> result = knowledgeArticleMyQueryService.getMyArticles(10L, request);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getMyRecentArticleHistory()")
    class GetMyRecentArticleHistory {

        @Test
        @DisplayName("Returns recent history list")
        void getMyRecentArticleHistory_success() {
            MyArticleHistoryDto history = new MyArticleHistoryDto();
            history.setId(1L);
            history.setTitle("최근 수정 문서");
            history.setArticleStatus(ArticleStatus.PENDING);
            history.setUpdatedAt(LocalDateTime.now());

            given(knowledgeArticleMapper.findMyRecentArticleHistory(10L)).willReturn(List.of(history));

            List<MyArticleHistoryDto> result = knowledgeArticleMyQueryService.getMyRecentArticleHistory(10L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("최근 수정 문서", result.get(0).getTitle());
        }
    }
}
