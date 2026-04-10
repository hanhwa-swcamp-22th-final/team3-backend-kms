package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MentoringRequestCreateRequest {

    @Positive(message = "신청자 ID는 양수여야 합니다.")
    private Long menteeId;

    @NotNull(message = "문서 ID는 필수입니다.")
    @Positive(message = "문서 ID는 양수여야 합니다.")
    private Long articleId;

    @NotBlank(message = "멘토링 분야는 필수입니다.")
    @Size(max = 100, message = "멘토링 분야는 100자 이하여야 합니다.")
    private String mentoringField;

    @NotBlank(message = "요청 제목은 필수입니다.")
    @Size(min = 2, max = 255, message = "요청 제목은 2자 이상 255자 이하여야 합니다.")
    private String requestTitle;

    @NotBlank(message = "요청 내용은 필수입니다.")
    @Size(min = 10, max = 1000, message = "요청 내용은 10자 이상 1000자 이하여야 합니다.")
    private String requestContent;

    private Integer mentoringDurationWeeks;
    private String mentoringFrequency;
    private RequestPriority requestPriority;
}
