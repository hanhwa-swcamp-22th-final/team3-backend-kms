package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleApprovalQueryService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public ApprovalArticleDetailDto getApprovalArticleById(Long articleId) {
        return knowledgeArticleMapper.findApprovalArticleById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
    }

    public List<ApprovalArticleDto> getApprovalArticles(ApprovalQueryRequest request) {
        normalizeRequest(request);
        return knowledgeArticleMapper.findApprovalArticles(request);
    }

    public ApprovalStatsDto getApprovalStats() {
        return knowledgeArticleMapper.findApprovalStats();
    }

    private void normalizeRequest(ApprovalQueryRequest request) {
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
