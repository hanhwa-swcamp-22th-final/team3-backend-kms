package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleAdminDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/admin/articles")
public class AdminArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* 지식 문서 삭제 (Admin) */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleAdminDeleteRequest request
    ) {
        knowledgeArticleService.adminDelete(articleId, request.getDeletionReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 지식 문서 수정 (Admin) */
    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminUpdate(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleUpdateRequest request
    ) {
        knowledgeArticleService.adminUpdate(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
