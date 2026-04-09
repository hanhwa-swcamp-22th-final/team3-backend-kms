package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmark;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmarkId;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBookmarkCommandService {

    private final KnowledgeBookmarkRepository bookmarkRepository;

    public void addBookmark(Long articleId, Long employeeId) {
        KnowledgeBookmarkId id = new KnowledgeBookmarkId(articleId, employeeId);
        if (bookmarkRepository.existsById(id)) {
            throw new BusinessException(ArticleErrorCode.BOOKMARK_001);
        }
        bookmarkRepository.save(KnowledgeBookmark.builder().id(id).build());
    }

    public void removeBookmark(Long articleId, Long employeeId) {
        KnowledgeBookmarkId id = new KnowledgeBookmarkId(articleId, employeeId);
        if (!bookmarkRepository.existsById(id)) {
            throw new BusinessException(ArticleErrorCode.BOOKMARK_002);
        }
        bookmarkRepository.deleteById(id);
    }
}
