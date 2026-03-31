package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleRejectRequest {

    private String reviewComment;   // 필수 (10~500자), Entity 비즈니스 로직에서 검증
}
