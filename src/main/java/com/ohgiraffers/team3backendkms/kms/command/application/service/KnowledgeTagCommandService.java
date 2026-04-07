package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgetag.KnowledgeTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeTagCommandService {

    private final KnowledgeTagRepository knowledgeTagRepository;
    private final IdGenerator idGenerator;

    public Long create(String tagName) {
        if (knowledgeTagRepository.existsByTagName(tagName)) {
            throw new BusinessException(ArticleErrorCode.TAG_001);
        }

        KnowledgeTag tag = KnowledgeTag.builder()
                .tagId(idGenerator.generate())
                .tagName(tagName)
                .build();

        return knowledgeTagRepository.save(tag).getTagId();
    }

    public void update(Long tagId, String tagName) {
        KnowledgeTag tag = findTagById(tagId);

        if (knowledgeTagRepository.existsByTagName(tagName)) {
            throw new BusinessException(ArticleErrorCode.TAG_001);
        }

        tag.updateTagName(tagName);
    }

    public void delete(Long tagId) {
        KnowledgeTag tag = findTagById(tagId);
        knowledgeTagRepository.delete(tag);
    }

    private KnowledgeTag findTagById(Long tagId) {
        return knowledgeTagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.TAG_NOT_FOUND));
    }
}
