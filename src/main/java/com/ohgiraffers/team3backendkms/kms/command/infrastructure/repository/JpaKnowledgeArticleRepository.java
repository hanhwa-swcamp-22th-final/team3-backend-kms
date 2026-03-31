package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaKnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
}
