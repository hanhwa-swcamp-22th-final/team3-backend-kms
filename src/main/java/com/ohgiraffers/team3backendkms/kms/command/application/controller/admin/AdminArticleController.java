package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.AdminArticleUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleAdminDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeTagCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/admin/articles")
public class AdminArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;
    private final KnowledgeTagCommandService knowledgeTagCommandService;

    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(
            @PathVariable @Positive(message = "ID must be positive.") Long articleId,
            @Valid @RequestBody ArticleAdminDeleteRequest request
    ) {
        knowledgeArticleCommandService.adminDelete(articleId, request.getDeletionReason());
        return ResponseEntity.ok(ApiResponse.success("Document deleted by admin.", null));
    }

    @PutMapping("/{articleId}/restore")
    public ResponseEntity<ApiResponse<Void>> adminRestore(
            @PathVariable @Positive(message = "ID must be positive.") Long articleId
    ) {
        knowledgeArticleCommandService.adminRestore(articleId);
        return ResponseEntity.ok(ApiResponse.success("Document restored by admin.", null));
    }

    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminUpdate(
            @PathVariable @Positive(message = "ID must be positive.") Long articleId,
            @Valid @RequestBody AdminArticleUpdateRequest request
    ) {
        knowledgeArticleCommandService.adminUpdate(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success("Document updated by admin.", null));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<Long>> createTag(
            @Valid @RequestBody KnowledgeTagCreateRequest request
    ) {
        Long tagId = knowledgeTagCommandService.create(request.getTagName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created.", tagId));
    }

    @PutMapping("/tags")
    public ResponseEntity<ApiResponse<Void>> updateTag(
            @Valid @RequestBody KnowledgeTagUpdateRequest request
    ) {
        knowledgeTagCommandService.update(request.getTagId(), request.getTagName());
        return ResponseEntity.ok(ApiResponse.success("Tag updated.", null));
    }

    @DeleteMapping("/tags")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Valid @RequestBody KnowledgeTagDeleteRequest request
    ) {
        knowledgeTagCommandService.delete(request.getTagId());
        return ResponseEntity.ok(ApiResponse.success("Tag deleted.", null));
    }
}
