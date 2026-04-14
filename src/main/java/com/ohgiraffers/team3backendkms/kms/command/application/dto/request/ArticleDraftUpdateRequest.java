package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleDraftUpdateRequest {

    private Long authorId;          // deprecated: JWT 인증 정보 우선 사용

    @Length(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    private ArticleCategory category;

    private Long equipmentId;

    @Length(max = 10000, message = "본문은 10,000자 이하여야 합니다")
    private String content;
}
