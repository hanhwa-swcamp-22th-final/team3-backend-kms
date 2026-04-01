package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    /* 지식 문서 등록 (PENDING) */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody ArticleRegisterRequest request) {
        Long articleId = knowledgeArticleService.register(
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
        Long articleId = knowledgeArticleService.draft(
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
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleUpdateRequest request
    ) {
        knowledgeArticleService.update(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getContent(),
                request.getAuthorId()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 지식 문서 삭제 (Worker) */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleDeleteRequest request
    ) {
        knowledgeArticleService.delete(articleId, request.getRequesterId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
