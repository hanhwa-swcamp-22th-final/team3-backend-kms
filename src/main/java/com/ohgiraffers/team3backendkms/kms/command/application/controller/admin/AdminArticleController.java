package com.ohgiraffers.team3backendkms.kms.command.application.controller.admin;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleAdminDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.AdminArticleUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeTagDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeTagCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/admin/articles")
public class AdminArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;
    private final KnowledgeTagCommandService knowledgeTagCommandService;

    /* 지식 문서 삭제 (Admin) */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleAdminDeleteRequest request
    ) {
        knowledgeArticleCommandService.adminDelete(articleId, request.getDeletionReason());
        return ResponseEntity.ok(ApiResponse.success("관리자 권한으로 문서를 삭제했습니다.", null));
    }

    /* 지식 문서 수정 (Admin) */
    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> adminUpdate(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody AdminArticleUpdateRequest request
    ) {
        knowledgeArticleCommandService.adminUpdate(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success("관리자 권한으로 문서를 수정했습니다.", null));
    }

    /* 지식 태그 등록 */
    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<Long>> createTag(
            @Valid @RequestBody KnowledgeTagCreateRequest request
    ) {
        Long tagId = knowledgeTagCommandService.create(request.getTagName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("태그가 생성되었습니다.", tagId));
    }

    /* 지식 태그 수정 */
    @PutMapping("/tags")
    public ResponseEntity<ApiResponse<Void>> updateTag(
            @Valid @RequestBody KnowledgeTagUpdateRequest request
    ) {
        knowledgeTagCommandService.update(request.getTagId(), request.getTagName());
        return ResponseEntity.ok(ApiResponse.success("태그가 수정되었습니다.", null));
    }

    /* 지식 태그 삭제 */
    @DeleteMapping("/tags")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Valid @RequestBody KnowledgeTagDeleteRequest request
    ) {
        knowledgeTagCommandService.delete(request.getTagId());
        return ResponseEntity.ok(ApiResponse.success("태그가 삭제되었습니다.", null));
    }
}
