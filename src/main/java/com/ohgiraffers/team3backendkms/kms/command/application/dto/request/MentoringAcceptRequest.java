package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MentoringAcceptRequest {

    @NotNull(message = "멘토 ID는 필수입니다")
    @Positive(message = "멘토 ID는 양수여야 합니다")
    private Long mentorId;

    // 임시: JWT 연결 전 역할 수동 전달
    private String mentorRole;  // WORKER / TL / DL
    private String mentorTier;  // S / A (WORKER인 경우)
}
