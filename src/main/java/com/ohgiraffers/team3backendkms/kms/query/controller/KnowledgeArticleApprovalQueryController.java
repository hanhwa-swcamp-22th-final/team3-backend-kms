package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleApprovalQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/approval")
public class KnowledgeArticleApprovalQueryController {

    private final KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ApprovalStatsDto>> getApprovalStats() {
        ApprovalStatsDto stats = knowledgeArticleApprovalQueryService.getApprovalStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
