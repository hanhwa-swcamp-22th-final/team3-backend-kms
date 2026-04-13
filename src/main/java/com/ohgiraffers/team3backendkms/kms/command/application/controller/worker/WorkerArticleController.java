package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDeleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;
    private final KnowledgeArticleTagCommandService knowledgeArticleTagCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(
            Authentication authentication,
            @Valid @RequestBody ArticleRegisterRequest request) {
        EmployeeUserDetails userDetails = currentUser(authentication);
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

    @PostMapping("/drafts")
    public ResponseEntity<ApiResponse<Long>> draft(
            Authentication authentication,
            @Valid @RequestBody ArticleDraftRequest request) {
        EmployeeUserDetails userDetails = currentUser(authentication);
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

    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> update(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleDraftUpdateRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
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

    @PutMapping("/{articleId}/draft")
    public ResponseEntity<ApiResponse<Void>> saveAsDraft(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleDraftUpdateRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
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

    @PutMapping("/{articleId}/revision")
    public ResponseEntity<ApiResponse<Long>> startRevision(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody(required = false) ArticleRevisionStartRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        Long revisionArticleId = knowledgeArticleCommandService.startRevision(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("수정본 작업을 위한 초안이 생성되었습니다.", revisionArticleId));
    }

    @PutMapping("/{articleId}/submit")
    public ResponseEntity<ApiResponse<Void>> submit(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleSubmitRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
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

    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody(required = false) ArticleDeleteRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        knowledgeArticleCommandService.delete(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("문서가 삭제되었습니다.", null));
    }

    @PutMapping("/{articleId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody(required = false) ArticleDeleteRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        knowledgeArticleCommandService.restore(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("문서가 복원되었습니다.", null));
    }

    @PutMapping("/{articleId}/tags")
    public ResponseEntity<ApiResponse<Void>> updateArticleTags(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody KnowledgeArticleTagUpdateRequest request
    ) {
        knowledgeArticleTagCommandService.updateArticleTags(articleId, request.getTagIds());
        return ResponseEntity.ok(ApiResponse.success("문서 태그가 수정되었습니다.", null));
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
