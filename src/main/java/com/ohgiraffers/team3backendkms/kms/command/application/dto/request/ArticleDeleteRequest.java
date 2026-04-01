package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDeleteRequest {

    @NotNull(message = "요청자 ID는 필수입니다")
    private Long requesterId;       // 임시: JWT 연결 시 제거 예정
}
