package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgeedithistory.KnowledgeEditHistory;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeEditHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleCommandService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeEditHistoryRepository knowledgeEditHistoryRepository;
    private final IdGenerator idGenerator;

    public Long register(Long authorId, Long equipmentId,
                         String title, ArticleCategory category, String content) {
        validateEquipmentId(equipmentId);
        return knowledgeArticleRepository.save(
                buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.PENDING)
        ).getArticleId();
    }

    public Long draft(Long authorId, Long equipmentId,
                      String title, ArticleCategory category, String content) {
        validateEquipmentIdIfPresent(equipmentId);
        return knowledgeArticleRepository.save(
                buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.DRAFT)
        ).getArticleId();
    }

    public void updateDraft(Long articleId, String title, ArticleCategory category, Long equipmentId, String content, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (article.getArticleStatus() != ArticleStatus.DRAFT
                && article.getArticleStatus() != ArticleStatus.PENDING
                && article.getArticleStatus() != ArticleStatus.REJECTED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_006);
        }
        if (!article.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_007);
        }
        validateEquipmentIdIfPresent(equipmentId);
        article.updateDraft(title, category, equipmentId, content);
    }

    public void submitDraft(Long articleId, String title, ArticleCategory category, Long equipmentId, String content, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (article.getArticleStatus() != ArticleStatus.DRAFT
                && article.getArticleStatus() != ArticleStatus.PENDING
                && article.getArticleStatus() != ArticleStatus.REJECTED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_SUBMIT_INVALID);
        }
        if (!article.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_007);
        }
        validateEquipmentId(equipmentId);
        article.updateDraft(title, category, equipmentId, content);
        article.submit();
    }

    public void startRevision(Long articleId, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (!article.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_007);
        }
        if (article.getArticleStatus() != ArticleStatus.APPROVED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_011);
        }

        Integer approvalVersion = article.getApprovalVersion();
        if (approvalVersion != null
                && !knowledgeEditHistoryRepository.existsByArticleIdAndApprovalVersion(articleId, approvalVersion)) {
            knowledgeEditHistoryRepository.save(KnowledgeEditHistory.from(idGenerator.generate(), article));
        }

        article.startRevision();
    }

    public void adminUpdate(Long articleId, String title, ArticleCategory category, String content) {
        KnowledgeArticle article = findArticleById(articleId);
        article.adminUpdate(title, category, content);
    }

    public void incrementViewCount(Long articleId) {
        KnowledgeArticle article = findArticleById(articleId);
        if (article.getArticleStatus() != ArticleStatus.APPROVED) {
            return;
        }
        article.incrementViewCount();
    }

    public void delete(Long articleId, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        if (article.getArticleStatus() == ArticleStatus.PENDING) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_010);
        }
        if (article.getArticleStatus() == ArticleStatus.REJECTED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_010);
        }
        if (article.getArticleStatus() == ArticleStatus.APPROVED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_009);
        }
        if (!article.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_007);
        }
        article.softDelete();
    }

    public void adminDelete(Long articleId, String reason) {
        KnowledgeArticle article = findArticleById(articleId);
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_008);
        }
        article.adminDelete(reason);
    }

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
        article.reject(reviewComment);
    }

    private KnowledgeArticle buildArticle(Long authorId, Long equipmentId,
                                          String title, ArticleCategory category,
                                          String content, ArticleStatus status) {
        return KnowledgeArticle.builder()
                .articleId(idGenerator.generate())
                .authorId(authorId)
                .equipmentId(equipmentId)
                .fileGroupId(0L)
                .articleTitle(title)
                .articleCategory(category)
                .articleContent(content)
                .articleStatus(status)
                .approvalVersion(0)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    private void validateEquipmentId(Long equipmentId) {
        if (equipmentId == null || equipmentId <= 0) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_005);
        }
    }

    private void validateEquipmentIdIfPresent(Long equipmentId) {
        if (equipmentId != null && equipmentId <= 0) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_005);
        }
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
    }
}
