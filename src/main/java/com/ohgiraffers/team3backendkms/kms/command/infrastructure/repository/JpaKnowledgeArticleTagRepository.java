package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaKnowledgeArticleTagRepository extends JpaRepository<KnowledgeArticleTag, KnowledgeArticleTagId> {

    List<KnowledgeArticleTag> findByIdArticleId(Long articleId);

    void deleteByIdArticleId(Long articleId);
}
