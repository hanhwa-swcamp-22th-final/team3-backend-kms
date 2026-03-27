package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeArticleQueryService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public List<ArticleReadDto> getArticles(ArticleQueryRequest request) {
        return knowledgeArticleMapper.findArticles(request);
    }

    public ArticleDetailDto getArticleDetail(Long articleId) {
        return knowledgeArticleMapper.findArticleById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. id=" + articleId));
    }
}
