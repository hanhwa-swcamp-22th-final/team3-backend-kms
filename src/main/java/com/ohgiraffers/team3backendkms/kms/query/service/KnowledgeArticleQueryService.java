package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
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

    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final KnowledgeTagMapper knowledgeTagMapper;

    public List<ArticleReadDto> getArticles(ArticleQueryRequest request) {
        normalizeQueryRequest(request);
        return knowledgeArticleMapper.findArticles(request);
    }

    public ArticleDetailDto getArticleDetail(Long articleId) {
        ArticleDetailDto detail = knowledgeArticleMapper.findArticleById(articleId)
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
        return knowledgeArticleMapper.findRecommendations();
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
}
