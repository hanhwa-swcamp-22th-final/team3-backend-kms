package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KnowledgeArticleTagUpdateRequest {

    @NotNull(message = "태그 ID 목록은 필수입니다")
    private List<Long> tagIds;
}
