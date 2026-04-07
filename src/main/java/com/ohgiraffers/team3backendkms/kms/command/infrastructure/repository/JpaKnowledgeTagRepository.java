package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgetag.KnowledgeTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaKnowledgeTagRepository extends JpaRepository<KnowledgeTag, Long> {

    boolean existsByTagName(String tagName);
}
