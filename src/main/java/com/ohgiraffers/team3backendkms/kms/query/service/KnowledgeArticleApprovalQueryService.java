package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleApprovalQueryService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public ApprovalStatsDto getApprovalStats() {
        return knowledgeArticleMapper.findApprovalStats();
    }
}
