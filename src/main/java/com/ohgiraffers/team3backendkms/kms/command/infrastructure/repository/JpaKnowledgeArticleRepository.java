package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaKnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findFirstByOriginalArticleIdAndAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long originalArticleId,
            Long authorId
    );
}
