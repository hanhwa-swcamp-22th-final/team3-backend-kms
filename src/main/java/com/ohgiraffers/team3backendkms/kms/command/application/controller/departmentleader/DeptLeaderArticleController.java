package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApproveRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class DeptLeaderArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* DL 최종 승인 (TL_APPROVED → APPROVED) */
    @PostMapping("/approval/{articleId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long articleId,
            @RequestBody ArticleApproveRequest request
    ) {
        knowledgeArticleService.approve(articleId, request.getApproverId(), request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* DL 반려 (TL_APPROVED → REJECTED) */
    @PostMapping("/approval/{articleId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long articleId,
            @RequestBody ArticleRejectRequest request
    ) {
        knowledgeArticleService.reject(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
