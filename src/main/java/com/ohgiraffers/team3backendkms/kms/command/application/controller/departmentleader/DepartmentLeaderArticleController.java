package com.ohgiraffers.team3backendkms.kms.command.application.controller.departmentleader;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.AuthenticatedEmployee;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleApprovalProcessRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/dl/articles")
public class DepartmentLeaderArticleController {

    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @PostMapping("/{articleId}/approval")
    public ResponseEntity<ApiResponse<Void>> processApproval(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestHeader(value = "X-Employee-Id", required = false) Long approverId,
            @Valid @RequestBody ArticleApprovalProcessRequest request
    ) {
        knowledgeArticleCommandService.processApproval(
                articleId,
                AuthenticatedEmployee.employeeId(userDetails, approverId),
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
}
