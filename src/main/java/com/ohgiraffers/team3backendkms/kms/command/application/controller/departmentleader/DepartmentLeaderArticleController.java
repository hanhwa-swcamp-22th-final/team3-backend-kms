package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApprovalProcessRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/dl/articles")
public class DepartmentLeaderArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @PostMapping("/{articleId}/approval")
    public ResponseEntity<ApiResponse<Void>> processApproval(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @Valid @RequestBody ArticleApprovalProcessRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        knowledgeArticleCommandService.processApproval(
                articleId,
                userDetails.getEmployeeId(),
                request.getStatus(),
                request.getReviewComment()
        );
        return ResponseEntity.ok(ApiResponse.success(buildApprovalMessage(request), null));
    }

    private String buildApprovalMessage(ArticleApprovalProcessRequest request) {
        return switch (request.getStatus()) {
            case APPROVE -> "부서장 승인 처리가 완료되었습니다.";
            case REJECT -> "부서장 반려 처리가 완료되었습니다.";
            case PENDING -> "부서장 보류 처리가 완료되었습니다.";
        };
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
