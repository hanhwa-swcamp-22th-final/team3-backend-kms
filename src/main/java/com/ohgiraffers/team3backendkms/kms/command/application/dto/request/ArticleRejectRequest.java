package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleRejectRequest {

    private String reviewComment;   // 필수 (10~500자), null·길이 검증은 Entity reject()에서 처리
}
