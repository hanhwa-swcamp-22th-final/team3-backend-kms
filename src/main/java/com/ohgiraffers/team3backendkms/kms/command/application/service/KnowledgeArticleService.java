package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final IdGenerator idGenerator;


    /* 지식 문서 등록 (PENDING) */
    public Long register(Long authorId, Long equipmentId,
                         String title, ArticleCategory category, String content) {
        validateInput(title, category, content);
        return knowledgeArticleRepository.save(
                buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.PENDING)
        ).getArticleId();
    }

    /* 지식 문서 임시저장 (DRAFT) — 임시저장은 길이 검증 없이 허용 */
    public Long draft(Long authorId, Long equipmentId,
                      String title, ArticleCategory category, String content) {
        return knowledgeArticleRepository.save(
                buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.DRAFT)
        ).getArticleId();
    }

    /* 지식 문서 수정 (Worker) */
    public void update(Long articleId, String title, ArticleCategory category, String content, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);

        if (!article.getAuthorId().equals(requesterId)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
        }

        validateInput(title, category, content);
        article.update(title, category, content);
    }

    /* 지식 문서 수정 (Admin) — 작성자 체크 없이 수정 가능 */
    public void adminUpdate(Long articleId, String title, ArticleCategory category, String content) {
        KnowledgeArticle article = findArticleById(articleId);
        validateInput(title, category, content);
        article.update(title, category, content);
    }

    /* 조회수 증가 */
    public void incrementViewCount(Long articleId) {
        KnowledgeArticle article = findArticleById(articleId);
        article.incrementViewCount();
    }

    /* 승인 (PENDING → APPROVED) */
    public void approve(Long articleId, Long approverId, String opinion) {
        KnowledgeArticle article = findArticleById(articleId);
        article.approve(approverId, opinion);
    }

    /* 지식 문서 반려 */
    public void reject(Long articleId, String reason) {
        KnowledgeArticle article = findArticleById(articleId);
        article.reject(reason);
    }

    /* 지식 문서 삭제 (Worker) — DRAFT 또는 본인 확인 후 삭제 */
    public void delete(Long articleId, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);

        if (!article.getAuthorId().equals(requesterId)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
        }

        article.softDelete();
    }

    /* 지식 문서 삭제 (Admin) — 모든 상태 삭제 가능, 삭제 사유 필수 */
    public void adminDelete(Long articleId, String reason) {
        KnowledgeArticle article = findArticleById(articleId);
        article.adminDelete(reason);
    }

    // =========================================================
    // private 헬퍼 메서드
    // =========================================================

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
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    private void validateInput(String title, ArticleCategory category, String content) {
        if (title == null || title.length() < 5 || title.length() > 200) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_001.getMessage());
        }
        if (category == null) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_004.getMessage());
        }
        if (content == null || content.length() < 50) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_002.getMessage());
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_003.getMessage());
        }
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND.getMessage()));
    }
}
