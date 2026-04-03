package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleApprovalService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    public void approve(Long articleId, Long approverId, String reviewComment) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (article.getArticleStatus() == ArticleStatus.APPROVED) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_005);
        }
        if (article.getArticleStatus() == ArticleStatus.REJECTED) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_006);
        }
        if (article.getArticleStatus() != ArticleStatus.PENDING) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_003);
        }
        if (reviewComment != null && reviewComment.length() > 500) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_002);
        }
        article.approve(approverId, reviewComment);
    }

    public void reject(Long articleId, String reviewComment) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (article.getArticleStatus() == ArticleStatus.REJECTED) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_007);
        }
        if (article.getArticleStatus() == ArticleStatus.APPROVED) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_008);
        }
        if (article.getArticleStatus() != ArticleStatus.PENDING) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_003);
        }
        if (reviewComment == null || reviewComment.length() < 10 || reviewComment.length() > 500) {
            throw new BusinessException(ArticleErrorCode.APPROVAL_001);
        }
        article.reject(reviewComment);
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
    }
}
