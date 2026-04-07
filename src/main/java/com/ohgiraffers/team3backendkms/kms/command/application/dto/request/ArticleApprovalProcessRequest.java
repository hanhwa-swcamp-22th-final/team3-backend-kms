package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleApprovalProcessRequest {

    @NotNull(message = "처리 상태는 필수입니다")
    private ApprovalStatus status;

    private String reviewComment;
}
