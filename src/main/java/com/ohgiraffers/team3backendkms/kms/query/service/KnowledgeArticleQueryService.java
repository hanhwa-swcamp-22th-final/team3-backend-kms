package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticlePageResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeHubStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final KnowledgeTagMapper knowledgeTagMapper;

    public List<ArticleReadDto> getArticles(ArticleQueryRequest request) {
        normalizeQueryRequest(request);
        List<ArticleReadDto> articles = knowledgeArticleMapper.findArticles(request);
        articles.forEach(article -> article.setTags(knowledgeTagMapper.findTagsByArticleId(article.getArticleId())));
        return articles;
    }

    public ArticlePageResponse getPagedArticles(ArticleQueryRequest request) {
        normalizeQueryRequest(request);
        normalizePageRequest(request);

        long totalElements = knowledgeArticleMapper.countArticles(request);
        List<ArticleReadDto> articles = totalElements > 0
                ? knowledgeArticleMapper.findArticles(request)
                : List.of();
        articles.forEach(article -> article.setTags(knowledgeTagMapper.findTagsByArticleId(article.getArticleId())));

        int size = request.getSize();
        int currentPage = request.getPage();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return ArticlePageResponse.builder()
                .items(articles)
                .page(currentPage)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(totalPages > 0 && currentPage < totalPages - 1)
                .hasPrevious(currentPage > 0)
                .build();
    }

    public ArticleDetailDto getArticleDetail(Long articleId, Long requesterId) {
        ArticleDetailDto detail = knowledgeArticleMapper.findArticleById(articleId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
        detail.setTags(knowledgeTagMapper.findTagsByArticleId(articleId));
        return detail;
    }

    public KnowledgeHubStatsDto getKnowledgeHubStats() {
        return knowledgeArticleMapper.findKnowledgeHubStats();
    }

    public List<ContributorRankDto> getTopContributors(Integer limit) {
        return knowledgeArticleMapper.findTopContributors(Map.of("limit", limit));
    }

    public List<ArticleReadDto> getRecommendations() {
        List<ArticleReadDto> recommendations = knowledgeArticleMapper.findRecommendations();
        recommendations.forEach(article -> article.setTags(knowledgeTagMapper.findTagsByArticleId(article.getArticleId())));
        return recommendations;
    }

    private void normalizeQueryRequest(ArticleQueryRequest request) {
        if (request == null) {
            return;
        }

        if ("articleId".equals(request.getSearchType()) && request.getKeyword() != null && !request.getKeyword().isBlank()) {
            try {
                request.setArticleIdKeyword(Long.parseLong(request.getKeyword().trim()));
            } catch (NumberFormatException e) {
                request.setArticleIdKeyword(-1L);
            }
            return;
        }

        request.setArticleIdKeyword(null);
    }

    private void normalizePageRequest(ArticleQueryRequest request) {
        if (request == null) {
            return;
        }

        int normalizedPage = request.getPage() == null ? DEFAULT_PAGE : Math.max(request.getPage(), 0);
        int normalizedSize = request.getSize() == null ? DEFAULT_SIZE : Math.min(Math.max(request.getSize(), 1), MAX_SIZE);

        request.setPage(normalizedPage);
        request.setSize(normalizedSize);
    }
}
