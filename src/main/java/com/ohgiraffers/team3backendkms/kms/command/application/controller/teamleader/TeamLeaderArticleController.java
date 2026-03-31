package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

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
public class TeamLeaderArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* TL 1차 승인 (PENDING → TL_APPROVED) */
    @PostMapping("/approval/{articleId}/tl-approve")
    public ResponseEntity<ApiResponse<Void>> tlApprove(
            @PathVariable Long articleId,
            @RequestBody ArticleApproveRequest request
    ) {
        knowledgeArticleService.tlApprove(articleId, request.getApproverId(), request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* TL 반려 (PENDING → REJECTED) */
    @PostMapping("/approval/{articleId}/tl-reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long articleId,
            @RequestBody ArticleRejectRequest request
    ) {
        knowledgeArticleService.reject(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
