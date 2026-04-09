package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmark;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark.KnowledgeBookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaKnowledgeBookmarkRepository
        extends JpaRepository<KnowledgeBookmark, KnowledgeBookmarkId> {
}
