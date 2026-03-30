package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
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

    private static final String ERR_TITLE_LENGTH   = "[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다.";
    private static final String ERR_CONTENT_SHORT  = "[ARTICLE_002] 본문은 50자 이상이어야 합니다.";
    private static final String ERR_CONTENT_LONG   = "[ARTICLE_003] 본문은 10,000자 이하여야 합니다.";
    private static final String ERR_NOT_AUTHOR     = "[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다.";
    private static final String ERR_ALREADY_DELETED = "[ARTICLE_008] 이미 삭제된 문서입니다.";
    private static final String ERR_NOT_FOUND      = "문서를 찾을 수 없습니다.";

    /* 지식 문서 등록 (PENDING) */
    public void register(Long authorId, Long equipmentId,
                         String title, ArticleCategory category, String content) {
        validateInput(title, content);
        knowledgeArticleRepository.save(buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.PENDING));
    }

    /* 지식 문서 임시저장 (DRAFT) — 임시저장은 길이 검증 없이 허용 */
    public void draft(Long authorId, Long equipmentId,
                      String title, ArticleCategory category, String content) {
        knowledgeArticleRepository.save(buildArticle(authorId, equipmentId, title, category, content, ArticleStatus.DRAFT));
    }

    /* 지식 문서 상세 조회 */
    public KnowledgeArticle getDetail(Long articleId) {
        KnowledgeArticle article = findArticleById(articleId);

        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new IllegalStateException(ERR_ALREADY_DELETED);
        }

        article.incrementViewCount();
        return article;
    }

    /* 지식 문서 승인 */
    public void approve(Long articleId, Long approverId, String opinion) {
        KnowledgeArticle article = findArticleById(articleId);
        article.approve(approverId, opinion);
    }

    /* 지식 문서 반려 */
    public void reject(Long articleId, String reason) {
        KnowledgeArticle article = findArticleById(articleId);
        article.reject(reason);
    }

    /* 지식 문서 삭제 */
    public void delete(Long articleId, Long requesterId) {
        KnowledgeArticle article = findArticleById(articleId);

        if (!article.getAuthorId().equals(requesterId)) {
            throw new IllegalStateException(ERR_NOT_AUTHOR);
        }

        article.softDelete();
    }

    // =========================================================
    // private 공통 메서드
    // =========================================================

    private KnowledgeArticle buildArticle(Long authorId, Long equipmentId,
                                          String title, ArticleCategory category,
                                          String content, ArticleStatus status) {
        return KnowledgeArticle.builder()
                .articleId(idGenerator.generate())
                .authorId(authorId)
                .equipmentId(equipmentId)
                .fileGroupId(0L) // TODO: 파일 그룹 연동 후 실제 fileGroupId로 교체
                .articleTitle(title)
                .articleCategory(category)
                .articleContent(content)
                .articleStatus(status)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    private void validateInput(String title, String content) {
        if (title == null || title.length() < 5 || title.length() > 200) {
            throw new IllegalArgumentException(ERR_TITLE_LENGTH);
        }
        if (content == null || content.length() < 50) {
            throw new IllegalArgumentException(ERR_CONTENT_SHORT);
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException(ERR_CONTENT_LONG);
        }
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ERR_NOT_FOUND));
    }
}
