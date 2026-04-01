package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleAdminDeleteRequest {

    @NotBlank(message = "삭제 사유는 필수입니다")
    private String deletionReason;
}
