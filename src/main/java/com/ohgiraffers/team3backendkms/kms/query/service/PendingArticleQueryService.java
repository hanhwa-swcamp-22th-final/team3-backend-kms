package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.PendingArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingArticleQueryService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public PendingArticleDetailDto getPendingArticleById(Long articleId) {
        return knowledgeArticleMapper.findPendingArticleById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
    }

    public List<PendingArticleDto> getPendingArticles(PendingArticleQueryRequest request) {
        normalizeRequest(request);
        return knowledgeArticleMapper.findPendingArticles(request);
    }

    public PendingArticleStatsDto getPendingStats() {
        return knowledgeArticleMapper.findPendingStats();
    }

    private void normalizeRequest(PendingArticleQueryRequest request) {
        if (request == null) {
            return;
        }
        // articleId 검색은 문자열 keyword 로 들어오므로 mapper 에 넘기기 전에 숫자 필드로 변환한다.
        if ("articleId".equals(request.getSearchType()) && request.getKeyword() != null && !request.getKeyword().isBlank()) {
            try {
                request.setArticleIdKeyword(Long.parseLong(request.getKeyword().trim()));
            } catch (NumberFormatException e) {
                // 숫자로 바뀌지 않으면 조회 결과가 없도록 음수 sentinel 값을 사용한다.
                request.setArticleIdKeyword(-1L);
            }
            return;
        }
        request.setArticleIdKeyword(null);
    }
}
