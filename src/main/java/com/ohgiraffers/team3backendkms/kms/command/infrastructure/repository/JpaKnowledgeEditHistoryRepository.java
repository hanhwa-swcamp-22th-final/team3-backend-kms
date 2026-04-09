package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgeedithistory.KnowledgeEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaKnowledgeEditHistoryRepository extends JpaRepository<KnowledgeEditHistory, Long> {

    boolean existsByArticleIdAndApprovalVersion(Long articleId, Integer approvalVersion);
}
