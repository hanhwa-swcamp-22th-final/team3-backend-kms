package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ApprovalStatus;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleReviewRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/dl/approval")
public class DepartmentLeaderArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @PostMapping("/{articleId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @RequestBody ArticleReviewRequest request,
            @RequestHeader("X-Employee-Id") Long approverId
    ) {
        knowledgeArticleCommandService.processApproval(
                articleId,
                approverId,
                ApprovalStatus.APPROVE,
                request.getReviewComment()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{articleId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @RequestBody ArticleReviewRequest request,
            @RequestHeader("X-Employee-Id") Long approverId
    ) {
        knowledgeArticleCommandService.processApproval(
                articleId,
                approverId,
                ApprovalStatus.REJECT,
                request.getReviewComment()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{articleId}/pending")
    public ResponseEntity<ApiResponse<Void>> pending(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @RequestBody ArticleReviewRequest request,
            @RequestHeader("X-Employee-Id") Long approverId
    ) {
        knowledgeArticleCommandService.processApproval(
                articleId,
                approverId,
                ApprovalStatus.PENDING,
                request.getReviewComment()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
