package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleHoldRequest {

    @NotBlank(message = "보류 의견은 필수입니다")
    @Length(max = 500, message = "보류 의견은 500자 이하여야 합니다")
    private String reviewComment;
}
