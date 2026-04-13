package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class MentoringRequestUpdateRequest {

    @NotNull(message = "신청자 ID는 필수입니다")
    @Positive(message = "신청자 ID는 양수여야 합니다")
    private Long menteeId;

    @NotBlank(message = "신청 제목은 필수입니다")
    @Length(min = 1, max = 255, message = "신청 제목은 1자 이상 255자 이하여야 합니다")
    private String requestTitle;

    @NotBlank(message = "신청 내용은 필수입니다")
    @Length(min = 10, max = 1000, message = "신청 내용은 10자 이상 1000자 이하여야 합니다")
    private String requestContent;
}
