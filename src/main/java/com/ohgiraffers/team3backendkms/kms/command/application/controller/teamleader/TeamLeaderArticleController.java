package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApproveRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/tl/approval")
public class TeamLeaderArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @PostMapping("/{articleId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleApproveRequest request
    ) {
        knowledgeArticleCommandService.approve(articleId, request.getApproverId(), request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{articleId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleRejectRequest request
    ) {
        knowledgeArticleCommandService.reject(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
