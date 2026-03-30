package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 쿼리 파라미터 바인딩에 필요 (예외적 Setter 허용)
@NoArgsConstructor
public class ArticleQueryRequest {

    private ArticleCategory category;   // 카테고리 필터
    private ArticleStatus status;       // 상태 필터
    private String sort;                // 정렬 (latest / popular)
    private Integer page;
    private Integer size;
}
