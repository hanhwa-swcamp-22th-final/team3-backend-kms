package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleApproveRequest {

    @Length(max = 500, message = "승인 의견은 500자 이하여야 합니다")
    private String reviewComment;   // nullable — 입력 시 최대 500자
}
