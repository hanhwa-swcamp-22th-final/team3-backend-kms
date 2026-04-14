package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDeleteRequest {

    private Long requesterId;       // deprecated: JWT 인증 정보 우선 사용
}
