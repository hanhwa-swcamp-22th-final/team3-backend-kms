package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleApproveRequest {

    @NotNull(message = "승인자 ID는 필수입니다")
    private Long approverId;        // 임시: JWT 연결 시 제거 예정

    @Length(max = 500, message = "승인 의견은 500자 이하여야 합니다")
    private String reviewComment;   // nullable — 입력 시 최대 500자
}
