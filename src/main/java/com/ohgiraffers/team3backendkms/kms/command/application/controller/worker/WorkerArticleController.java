package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleSubmitRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeArticleTagUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleTagCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;
    private final KnowledgeArticleTagCommandService knowledgeArticleTagCommandService;

    /* 지식 문서 등록 (PENDING) */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody ArticleRegisterRequest request) {
        Long articleId = knowledgeArticleCommandService.register(
                request.getAuthorId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(articleId));
    }

    /* 지식 문서 임시저장 (DRAFT) */
    @PostMapping("/drafts")
    public ResponseEntity<ApiResponse<Long>> draft(@Valid @RequestBody ArticleDraftRequest request) {
        Long articleId = knowledgeArticleCommandService.draft(
                request.getAuthorId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(articleId));
    }

    /* 지식 문서 수정 (Worker) */
    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleDraftUpdateRequest request
    ) {
        knowledgeArticleCommandService.updateDraft(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getEquipmentId(),
                request.getContent(),
                request.getAuthorId()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 임시저장 문서 제출 (DRAFT -> PENDING) */
    @PutMapping("/{articleId}/submit")
    public ResponseEntity<ApiResponse<Void>> submit(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleSubmitRequest request
    ) {
        knowledgeArticleCommandService.submitDraft(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getEquipmentId(),
                request.getContent(),
                request.getAuthorId()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 지식 문서 삭제 (Worker) */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleDeleteRequest request
    ) {
        knowledgeArticleCommandService.delete(articleId, request.getRequesterId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 게시글 태그 연결 (전체 교체) */
    @PutMapping("/{articleId}/tags")
    public ResponseEntity<ApiResponse<Void>> updateArticleTags(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody KnowledgeArticleTagUpdateRequest request
    ) {
        knowledgeArticleTagCommandService.updateArticleTags(articleId, request.getTagIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
