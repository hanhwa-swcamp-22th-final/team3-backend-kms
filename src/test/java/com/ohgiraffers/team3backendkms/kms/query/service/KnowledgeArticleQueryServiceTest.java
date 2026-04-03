package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleQueryServiceTest {

    @InjectMocks
    private KnowledgeArticleQueryService knowledgeArticleQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Nested
    // getArticles 메서드
    @DisplayName("getArticles()")
    class GetArticles {

        @Test
        // 지식 목록 조회 성공: 목록 응답 DTO를 반환한다
        @DisplayName("Returns list of ArticleReadDto")
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
        // 지식 목록 조회 성공: 데이터가 없으면 빈 목록을 반환한다
        @DisplayName("Returns empty list when no data")
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
    // getArticleDetail 메서드
    @DisplayName("getArticleDetail()")
    class GetArticleDetail {

        @Test
        // 지식 상세 조회 성공: 상세 응답 DTO를 반환한다
        @DisplayName("Returns ArticleDetailDto")
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
            given(knowledgeArticleMapper.findArticleById(1L)).willReturn(Optional.of(dto));

            // when
            ArticleDetailDto result = knowledgeArticleQueryService.getArticleDetail(1L);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getArticleId());
            assertEquals("테스트 제목입니다", result.getArticleTitle());
            assertEquals(ArticleStatus.APPROVED, result.getArticleStatus());
        }

        @Test
        // 지식 상세 조회 실패: 문서가 없으면 예외가 발생한다
        @DisplayName("Throws exception when article not found")
        void getArticleDetail_whenNotFound_thenThrow() {
            // given
            given(knowledgeArticleMapper.findArticleById(999L)).willReturn(Optional.empty());

            // when & then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> knowledgeArticleQueryService.getArticleDetail(999L)
            );
            assertTrue(exception.getMessage().contains("문서를 찾을 수 없습니다"));
        }
    }

    @Nested
    // getTopContributors 메서드
    @DisplayName("getTopContributors()")
    class GetTopContributors {

        @Test
        // 월간 기여자 랭킹 조회 성공: limit 개수만큼 반환한다
        @DisplayName("Returns top contributors list with specified limit")
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
        // 월간 기여자 랭킹 조회: 데이터가 없으면 빈 목록을 반환한다
        @DisplayName("Returns empty list when no contributors")
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
    // getRecommendations 메서드
    @DisplayName("getRecommendations()")
    class GetRecommendations {

        @Test
        // AI 지식 추천 조회 성공: TOP 5 목록을 반환한다
        @DisplayName("Returns top 5 recommendations list")
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
        // AI 지식 추천 조회: 데이터가 없으면 빈 목록을 반환한다
        @DisplayName("Returns empty list when no recommendations")
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
