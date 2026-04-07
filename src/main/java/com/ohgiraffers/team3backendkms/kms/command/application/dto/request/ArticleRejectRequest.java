package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleRejectRequest {

    @NotNull
    @Length(min = 10, max = 500, message = "반려 사유는 10자 이상 500자 이하여야 합니다")
    private String reviewComment;   // 필수 (10~500자) — DTO에서 검증
}
