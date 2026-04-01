package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleAdminDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/admin")
public class AdminArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* 지식 문서 삭제 (Admin) — 모든 상태 삭제 가능, 삭제 사유 필수 */
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleAdminDeleteRequest request
    ) {
        knowledgeArticleService.adminDelete(articleId, request.getDeletionReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
