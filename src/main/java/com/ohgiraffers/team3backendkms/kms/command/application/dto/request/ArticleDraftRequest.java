package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class ArticleDraftRequest {

    private Long authorId;          // 임시: JWT 연결 시 제거 예정
    @Length(min = 5, max = 200, message = "제목은 5자 이상 200자 이하여야 합니다")
    private String title;           // nullable — 입력 시 길이 검증
    private ArticleCategory category; // nullable
    private Long equipmentId;       // nullable
    @Length(min = 50, max = 10000, message = "본문은 50자 이상 10,000자 이하여야 합니다")
    private String content;         // nullable — 입력 시 길이 검증
}
