package com.ohgiraffers.team3backendkms.kms.command.application.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApproveRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleCommandController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* 지식 문서 등록 (PENDING) */
    @PreAuthorize("hasAnyAuthority('WORKER')")
    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<Long>> register(@RequestBody ArticleRegisterRequest request) {
        Long articleId = knowledgeArticleService.register(
                request.getAuthorId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success(articleId));
    }

    /* 지식 문서 임시저장 (DRAFT) */
    @PreAuthorize("hasAnyAuthority('WORKER')")
    @PostMapping("/articles/drafts")
    public ResponseEntity<ApiResponse<Long>> draft(@RequestBody ArticleDraftRequest request) {
        Long articleId = knowledgeArticleService.draft(
                request.getAuthorId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success(articleId));
    }

    /* 지식 문서 승인 */
    @PreAuthorize("hasAnyAuthority('TL', 'DL')")
    @PostMapping("/approval/{articleId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long articleId,
            @RequestBody ArticleApproveRequest request
    ) {
        knowledgeArticleService.approve(articleId, request.getApproverId(), request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 지식 문서 반려 */
    @PreAuthorize("hasAnyAuthority('TL', 'DL')")
    @PostMapping("/approval/{articleId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long articleId,
            @RequestBody ArticleRejectRequest request
    ) {
        knowledgeArticleService.reject(articleId, request.getReviewComment());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 지식 문서 삭제 (soft delete) */
    @PreAuthorize("hasAnyAuthority('WORKER')")
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long articleId,
            @RequestParam Long requesterId
    ) {
        knowledgeArticleService.delete(articleId, requesterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
