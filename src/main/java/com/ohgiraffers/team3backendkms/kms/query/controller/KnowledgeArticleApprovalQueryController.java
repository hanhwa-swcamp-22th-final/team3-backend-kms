package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleApprovalQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/approval")
public class KnowledgeArticleApprovalQueryController {

    private final KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;

    @GetMapping("/{articleId}")
    public ResponseEntity<ApiResponse<ApprovalArticleDetailDto>> getApprovalArticleById(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId
    ) {
        ApprovalArticleDetailDto detail = knowledgeArticleApprovalQueryService.getApprovalArticleById(articleId);
        return ResponseEntity.ok(ApiResponse.success("승인 대상 문서 상세를 조회했습니다.", detail));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApprovalArticleDto>>> getApprovalArticles(
            @ModelAttribute ApprovalQueryRequest request
    ) {
        List<ApprovalArticleDto> articles = knowledgeArticleApprovalQueryService.getApprovalArticles(request);
        return ResponseEntity.ok(ApiResponse.success("승인 대기 문서 목록을 조회했습니다.", articles));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ApprovalStatsDto>> getApprovalStats() {
        ApprovalStatsDto stats = knowledgeArticleApprovalQueryService.getApprovalStats();
        return ResponseEntity.ok(ApiResponse.success("승인 통계를 조회했습니다.", stats));
    }
}
