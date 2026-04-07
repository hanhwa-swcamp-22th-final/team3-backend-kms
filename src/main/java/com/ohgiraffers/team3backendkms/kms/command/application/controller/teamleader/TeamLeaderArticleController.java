package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApprovalProcessRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/tl/articles")
public class TeamLeaderArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @PostMapping("/{articleId}/approval")
    public ResponseEntity<ApiResponse<Void>> processApproval(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @RequestHeader("X-Employee-Id") Long approverId,
            @Valid @RequestBody ArticleApprovalProcessRequest request
    ) {
        knowledgeArticleCommandService.processApproval(
                articleId,
                approverId,
                request.getStatus(),
                request.getReviewComment()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
