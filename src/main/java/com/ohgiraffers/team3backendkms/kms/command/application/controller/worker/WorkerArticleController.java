package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRevisionStartRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleSubmitRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.KnowledgeArticleTagUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleTagCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;
    private final KnowledgeArticleTagCommandService knowledgeArticleTagCommandService;

    /* 지식 문서 등록 (PENDING) */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleRegisterRequest request) {
        Long articleId = knowledgeArticleCommandService.register(
                userDetails.getEmployeeId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("문서 등록이 완료되었고 승인 대기 상태로 접수되었습니다.", articleId));
    }

    /* 지식 문서 임시저장 (DRAFT) */
    @PostMapping("/drafts")
    public ResponseEntity<ApiResponse<Long>> draft(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleDraftRequest request) {
        Long articleId = knowledgeArticleCommandService.draft(
                userDetails.getEmployeeId(),
                request.getEquipmentId(),
                request.getTitle(),
                request.getCategory(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("문서가 임시저장되었습니다.", articleId));
    }

    /* 지식 문서 수정 (Worker) */
    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleDraftUpdateRequest request
    ) {
        knowledgeArticleCommandService.updateDraft(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getEquipmentId(),
                request.getContent(),
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("문서가 수정되었습니다.", null));
    }

    /* 지식 문서 임시저장 전환 (DRAFT/PENDING/REJECTED -> DRAFT) */
    @PutMapping("/{articleId}/draft")
    public ResponseEntity<ApiResponse<Void>> saveAsDraft(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleDraftUpdateRequest request
    ) {
        knowledgeArticleCommandService.saveAsDraft(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getEquipmentId(),
                request.getContent(),
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("문서가 임시저장 상태로 변경되었습니다.", null));
    }

    /* 승인된 지식 문서 수정 시작 (복사본 생성/조회) */
    @PutMapping("/{articleId}/revision")
    public ResponseEntity<ApiResponse<Long>> startRevision(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleRevisionStartRequest request
    ) {
        Long revisionArticleId = knowledgeArticleCommandService.startRevision(
                articleId,
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("수정본 작업을 위한 초안이 생성되었습니다.", revisionArticleId));
    }

    /* 임시저장 문서 제출 (DRAFT -> PENDING) */
    @PutMapping("/{articleId}/submit")
    public ResponseEntity<ApiResponse<Void>> submit(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleSubmitRequest request
    ) {
        knowledgeArticleCommandService.submitDraft(
                articleId,
                request.getTitle(),
                request.getCategory(),
                request.getEquipmentId(),
                request.getContent(),
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("임시저장 문서가 제출되었고 승인 대기 상태로 변경되었습니다.", null));
    }

    /* 지식 문서 삭제 (Worker) */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleDeleteRequest request
    ) {
        knowledgeArticleCommandService.delete(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("문서가 삭제되었습니다.", null));
    }

    @PutMapping("/{articleId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody ArticleDeleteRequest request
    ) {
        knowledgeArticleCommandService.restore(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("문서가 복원되었습니다.", null));
    }

    /* 게시글 태그 연결 (전체 교체) */
    @PutMapping("/{articleId}/tags")
    public ResponseEntity<ApiResponse<Void>> updateArticleTags(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody KnowledgeArticleTagUpdateRequest request
    ) {
        knowledgeArticleTagCommandService.updateArticleTags(articleId, request.getTagIds());
        return ResponseEntity.ok(ApiResponse.success("문서 태그가 수정되었습니다.", null));
    }
}
