package com.ohgiraffers.team3backendkms.kms.command.domain.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
}
