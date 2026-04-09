package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTagId;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleTagRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleTagCommandService {

    private final KnowledgeArticleTagRepository knowledgeArticleTagRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeTagRepository knowledgeTagRepository;

    public void updateArticleTags(Long articleId, List<Long> tagIds) {
        if (!knowledgeArticleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND);
        }

        for (Long tagId : tagIds) {
            if (!knowledgeTagRepository.existsById(tagId)) {
                throw new ResourceNotFoundException(ArticleErrorCode.TAG_NOT_FOUND);
            }
        }

        List<KnowledgeArticleTag> existing = knowledgeArticleTagRepository.findByIdArticleId(articleId);

        if (existing.isEmpty()) {
            List<KnowledgeArticleTag> toInsert = tagIds.stream()
                    .map(tagId -> KnowledgeArticleTag.builder()
                            .id(new KnowledgeArticleTagId(tagId, articleId))
                            .build())
                    .toList();
            knowledgeArticleTagRepository.saveAll(toInsert);
        } else {
            Set<Long> existingTagIds = existing.stream()
                    .map(at -> at.getId().getTagId())
                    .collect(Collectors.toSet());
            Set<Long> newTagIds = new HashSet<>(tagIds);

            List<KnowledgeArticleTag> toRemove = existing.stream()
                    .filter(at -> !newTagIds.contains(at.getId().getTagId()))
                    .toList();

            List<KnowledgeArticleTag> toAdd = tagIds.stream()
                    .filter(tagId -> !existingTagIds.contains(tagId))
                    .map(tagId -> KnowledgeArticleTag.builder()
                            .id(new KnowledgeArticleTagId(tagId, articleId))
                            .build())
                    .toList();

            knowledgeArticleTagRepository.deleteAll(toRemove);
            knowledgeArticleTagRepository.saveAll(toAdd);
        }
    }
}
