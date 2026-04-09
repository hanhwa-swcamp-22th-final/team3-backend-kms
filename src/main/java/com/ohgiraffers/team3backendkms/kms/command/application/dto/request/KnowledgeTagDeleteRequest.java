package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KnowledgeTagDeleteRequest {

    @NotNull(message = "태그 ID는 필수입니다")
    private Long tagId;
}
