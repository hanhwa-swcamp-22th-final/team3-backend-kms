package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleUpdateRequest {

    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;          // 임시: JWT 연결 시 제거 예정

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotNull(message = "카테고리는 필수입니다")
    private ArticleCategory category;

    @NotBlank(message = "본문은 필수입니다")
    private String content;
}
