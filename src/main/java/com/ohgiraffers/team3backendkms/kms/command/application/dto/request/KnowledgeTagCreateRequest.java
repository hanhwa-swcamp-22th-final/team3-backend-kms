package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class KnowledgeTagCreateRequest {

    @NotBlank(message = "태그 이름은 필수입니다")
    @Length(max = 255, message = "태그 이름은 255자 이하여야 합니다")
    private String tagName;
}
