package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MentoringCompleteRequest {

    @NotNull(message = "멘토 ID는 필수입니다")
    @Positive(message = "멘토 ID는 양수여야 합니다")
    private Long mentorId;
}
