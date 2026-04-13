package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmark;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmarkId;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 북마크 Command 서비스 — 북마크 추가 / 취소
 * 쓰기 작업만 담당 (읽기는 KnowledgeBookmarkQueryService)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBookmarkCommandService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeBookmarkRepository bookmarkRepository;

    /**
     * 북마크 추가
     * - 이미 북마크한 게시글이면 BOOKMARK_001 예외
     */
    public void addBookmark(Long articleId, Long employeeId) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND));
        if (Boolean.TRUE.equals(article.getIsDeleted()) || article.getArticleStatus() != ArticleStatus.APPROVED) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_NOT_FOUND);
        }

        KnowledgeBookmarkId id = new KnowledgeBookmarkId(articleId, employeeId);
        // 중복 북마크 방지
        if (bookmarkRepository.existsById(id)) {
            throw new BusinessException(ArticleErrorCode.BOOKMARK_001);
        }
        bookmarkRepository.save(KnowledgeBookmark.builder().id(id).build());
    }

    /**
     * 북마크 취소
     * - 존재하지 않는 북마크면 BOOKMARK_002 예외
     */
    public void removeBookmark(Long articleId, Long employeeId) {
        KnowledgeBookmarkId id = new KnowledgeBookmarkId(articleId, employeeId);
        // 북마크 존재 여부 확인
        if (!bookmarkRepository.existsById(id)) {
            throw new BusinessException(ArticleErrorCode.BOOKMARK_002);
        }
        bookmarkRepository.deleteById(id);
    }
}
