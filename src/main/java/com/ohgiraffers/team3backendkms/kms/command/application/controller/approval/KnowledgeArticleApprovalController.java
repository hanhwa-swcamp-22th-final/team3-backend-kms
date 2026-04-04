package com.ohgiraffers.team3backendkms.kms.command.application.controller.approval;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleHoldRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleApprovalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/approval")
public class KnowledgeArticleApprovalController {

    private final KnowledgeArticleApprovalService knowledgeArticleApprovalService;

    @PostMapping("/{articleId}/hold")
    public ResponseEntity<ApiResponse<Void>> hold(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleHoldRequest request
    ) {
        knowledgeArticleApprovalService.hold(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
