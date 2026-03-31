package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleApproveRequest {

    @NotNull(message = "승인자 ID는 필수입니다")
    private Long approverId;        // 임시: JWT 연결 시 제거 예정

    private String reviewComment;   // nullable (최대 500자)
}
