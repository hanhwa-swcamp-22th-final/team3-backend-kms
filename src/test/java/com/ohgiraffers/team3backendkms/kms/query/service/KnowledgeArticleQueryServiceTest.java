package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleQueryRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleQueryServiceTest {

    @InjectMocks
    private KnowledgeArticleQueryService knowledgeArticleQueryService;

    @Mock
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Nested
    @DisplayName("getArticles 메서드")
    class GetArticles {

        @Test
        @DisplayName("지식 목록 조회 성공: 목록 응답 DTO를 반환한다")
        void getArticles_success() {
            // given
            ArticleQueryRequest request = new ArticleQueryRequest();
            ArticleReadDto dto = new ArticleReadDto(
                    1L, 10L, "홍길동", "테스트 제목입니다",
                    ArticleCategory.TROUBLESHOOTING, ArticleStatus.APPROVED,
                    0, LocalDateTime.now()
            );
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
        @DisplayName("지식 목록 조회 성공: 데이터가 없으면 빈 목록을 반환한다")
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
    }

    @Nested
    @DisplayName("getArticleDetail 메서드")
    class GetArticleDetail {

        @Test
        @DisplayName("지식 상세 조회 성공: 상세 응답 DTO를 반환한다")
        void getArticleDetail_success() {
            // given
            ArticleDetailDto dto = new ArticleDetailDto(
                    1L, 10L, "홍길동", "테스트 제목입니다",
                    ArticleCategory.TROUBLESHOOTING, "본문 내용이 들어갑니다.",
                    ArticleStatus.APPROVED, "승인 의견", null,
                    5, LocalDateTime.now(), LocalDateTime.now()
            );
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
        @DisplayName("지식 상세 조회 실패: 문서가 없으면 예외가 발생한다")
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
}
