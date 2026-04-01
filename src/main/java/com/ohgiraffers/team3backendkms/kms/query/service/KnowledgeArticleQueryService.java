package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
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

    public List<ArticleReadDto> getArticles(ArticleQueryRequest request) {
        return knowledgeArticleMapper.findArticles(request);
    }

    public ArticleDetailDto getArticleDetail(Long articleId) {
        return knowledgeArticleMapper.findArticleById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다. id=" + articleId));
    }

    public List<ContributorRankDto> getTopContributors(Integer limit) {
        return knowledgeArticleMapper.findTopContributors(Map.of("limit", limit));
    }

    public List<ArticleReadDto> getRecommendations() {
        return knowledgeArticleMapper.findRecommendations();
    }
}
