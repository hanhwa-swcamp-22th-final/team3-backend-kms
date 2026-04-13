package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleQueryServiceTest {

    @InjectMocks
    private KnowledgeArticleQueryService knowledgeArticleQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Mock
    private KnowledgeTagMapper knowledgeTagMapper;

    @Nested
    @DisplayName("getArticles()")
    class GetArticles {

        @Test
        @DisplayName("Returns article list")
        void getArticles_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            ArticleReadDto dto = new ArticleReadDto();
            dto.setArticleId(1L);
            dto.setAuthorId(10L);
            dto.setAuthorName("홍길동");
            dto.setArticleTitle("테스트 제목입니다");
            dto.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto.setArticleStatus(ArticleStatus.APPROVED);
            dto.setViewCount(0);
            dto.setCreatedAt(LocalDateTime.now());
            given(knowledgeArticleMapper.findArticles(request)).willReturn(List.of(dto));

            // when
            List<ArticleReadDto> result = knowledgeArticleQueryService.getArticles(request);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("테스트 제목입니다", result.get(0).getArticleTitle());
            assertEquals("홍길동", result.get(0).getAuthorName());
        }

        @Test
        @DisplayName("Returns empty list when no data exists")
        void getArticles_whenNoData_thenReturnEmptyList() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            given(knowledgeArticleMapper.findArticles(request)).willReturn(List.of());

            // when
            List<ArticleReadDto> result = knowledgeArticleQueryService.getArticles(request);

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Converts articleId keyword to articleIdKeyword")
        void getArticles_withArticleIdKeyword_normalizesRequest() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            request.setSearchType("articleId");
            request.setKeyword("123");
            given(knowledgeArticleMapper.findArticles(request)).willReturn(List.of());

            // when
            knowledgeArticleQueryService.getArticles(request);

            // then
            assertEquals(123L, request.getArticleIdKeyword());
        }
    }

    @Nested
    @DisplayName("getArticleDetail()")
    class GetArticleDetail {

        @Test
        @DisplayName("Returns article detail")
        void getArticleDetail_success() {
            // given
            ArticleDetailDto dto = new ArticleDetailDto();
            dto.setArticleId(1L);
            dto.setAuthorId(10L);
            dto.setAuthorName("홍길동");
            dto.setArticleTitle("테스트 제목입니다");
            dto.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto.setArticleContent("본문 내용이 들어갑니다.");
            dto.setArticleStatus(ArticleStatus.APPROVED);
            dto.setArticleApprovalOpinion("승인 의견");
            dto.setViewCount(5);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setUpdatedAt(LocalDateTime.now());
            KnowledgeTagReadDto tag = new KnowledgeTagReadDto();
            tag.setTagId(100L);
            tag.setTagName("가공");
            given(knowledgeArticleMapper.findArticleById(1L, null)).willReturn(Optional.of(dto));
            given(knowledgeTagMapper.findTagsByArticleId(1L)).willReturn(List.of(tag));

            // when
            ArticleDetailDto result = knowledgeArticleQueryService.getArticleDetail(1L, null);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getArticleId());
            assertEquals("테스트 제목입니다", result.getArticleTitle());
            assertEquals(ArticleStatus.APPROVED, result.getArticleStatus());
            assertEquals(1, result.getTags().size());
            assertEquals("가공", result.getTags().get(0).getTagName());
        }

        @Test
        @DisplayName("Throws exception when article is not found")
        void getArticleDetail_whenNotFound_thenThrow() {
            // given
            given(knowledgeArticleMapper.findArticleById(999L, null)).willReturn(Optional.empty());

            // when & then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> knowledgeArticleQueryService.getArticleDetail(999L, null)
            );
            assertTrue(exception.getMessage().contains("문서를 찾을 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("getTopContributors()")
    class GetTopContributors {

        @Test
        @DisplayName("Returns top contributors with requested limit")
        void getTopContributors_success() {
            // given
            ContributorRankDto dto1 = new ContributorRankDto();
            dto1.setEmployeeId(1L);
            dto1.setEmployeeName("홍길동");
            dto1.setArticleCount(5L);
            dto1.setRank(1);

            ContributorRankDto dto2 = new ContributorRankDto();
            dto2.setEmployeeId(2L);
            dto2.setEmployeeName("김영희");
            dto2.setArticleCount(3L);
            dto2.setRank(2);

            given(knowledgeArticleMapper.findTopContributors(any(Map.class)))
                    .willReturn(List.of(dto1, dto2));

            // when
            List<ContributorRankDto> result = knowledgeArticleQueryService.getTopContributors(3);

            // then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("홍길동", result.get(0).getEmployeeName());
            assertEquals(5L, result.get(0).getArticleCount());
            assertEquals(1, result.get(0).getRank());
        }

        @Test
        @DisplayName("Returns empty list when no contributor exists")
        void getTopContributors_whenNoData_thenReturnEmptyList() {
            // given
            given(knowledgeArticleMapper.findTopContributors(any(Map.class)))
                    .willReturn(List.of());

            // when
            List<ContributorRankDto> result = knowledgeArticleQueryService.getTopContributors(3);

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getRecommendations()")
    class GetRecommendations {

        @Test
        @DisplayName("Returns recommendation list")
        void getRecommendations_success() {
            // given
            ArticleReadDto dto1 = new ArticleReadDto();
            dto1.setArticleId(1L);
            dto1.setAuthorId(10L);
            dto1.setAuthorName("홍길동");
            dto1.setArticleTitle("인기 있는 문서 1");
            dto1.setArticleCategory(ArticleCategory.TROUBLESHOOTING);
            dto1.setArticleStatus(ArticleStatus.APPROVED);
            dto1.setViewCount(100);
            dto1.setCreatedAt(LocalDateTime.now());

            ArticleReadDto dto2 = new ArticleReadDto();
            dto2.setArticleId(2L);
            dto2.setAuthorId(11L);
            dto2.setAuthorName("김영희");
            dto2.setArticleTitle("인기 있는 문서 2");
            dto2.setArticleCategory(ArticleCategory.PROCESS_IMPROVEMENT);
            dto2.setArticleStatus(ArticleStatus.APPROVED);
            dto2.setViewCount(50);
            dto2.setCreatedAt(LocalDateTime.now());

            given(knowledgeArticleMapper.findRecommendations())
                    .willReturn(List.of(dto1, dto2));

            // when
            List<ArticleReadDto> result = knowledgeArticleQueryService.getRecommendations();

            // then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(100, result.get(0).getViewCount());
            assertEquals(50, result.get(1).getViewCount());
        }

        @Test
        @DisplayName("Returns empty list when no recommendation exists")
        void getRecommendations_whenNoData_thenReturnEmptyList() {
            // given
            given(knowledgeArticleMapper.findRecommendations())
                    .willReturn(List.of());

            // when
            List<ArticleReadDto> result = knowledgeArticleQueryService.getRecommendations();

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
