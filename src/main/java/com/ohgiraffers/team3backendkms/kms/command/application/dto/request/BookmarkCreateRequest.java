package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkCreateRequest {

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long articleId;

    @NotNull(message = "직원 ID는 필수입니다.")
    private Long employeeId;
}
