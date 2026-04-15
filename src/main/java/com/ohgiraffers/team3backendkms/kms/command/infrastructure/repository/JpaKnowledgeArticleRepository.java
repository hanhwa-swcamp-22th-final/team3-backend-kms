package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaKnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findFirstByOriginalArticleIdAndAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long originalArticleId,
            Long authorId
    );

    boolean existsByAuthorIdAndEquipmentIdAndArticleTitleAndArticleCategoryAndArticleContentAndArticleStatusAndIsDeletedFalseAndCreatedAtAfter(
            Long authorId,
            Long equipmentId,
            String articleTitle,
            ArticleCategory articleCategory,
            String articleContent,
            ArticleStatus articleStatus,
            LocalDateTime createdAt
    );
}
