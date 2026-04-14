package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleRegisterRequest {

    private Long authorId;          // deprecated: JWT 인증 정보 우선 사용

    @NotBlank(message = "제목은 필수입니다")
    @Length(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다")
    private String title;

    @NotNull(message = "카테고리는 필수입니다")
    private ArticleCategory category;

    private Long equipmentId;       // nullable

    @NotBlank(message = "본문은 필수입니다")
    @Length(min = 50, max = 10000, message = "본문은 50자 이상 10,000자 이하여야 합니다")
    private String content;
}
