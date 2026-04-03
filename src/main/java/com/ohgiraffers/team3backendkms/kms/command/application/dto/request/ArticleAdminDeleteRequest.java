package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleAdminDeleteRequest {

    @NotBlank(message = "삭제 사유는 필수입니다")
    @Length(min = 10, max = 500, message = "삭제 사유는 10자 이상 500자 이하여야 합니다")
    private String deletionReason;
}
