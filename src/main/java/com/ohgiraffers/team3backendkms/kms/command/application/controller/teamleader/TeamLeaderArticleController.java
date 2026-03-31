package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApproveRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class TeamLeaderArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* TL 승인 (PENDING → APPROVED) */
    @PostMapping("/tl/approval/{articleId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleApproveRequest request
    ) {
        knowledgeArticleService.approve(articleId, request.getApproverId(), request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* TL 반려 (PENDING → REJECTED) */
    @PostMapping("/tl/approval/{articleId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleRejectRequest request
    ) {
        knowledgeArticleService.reject(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
