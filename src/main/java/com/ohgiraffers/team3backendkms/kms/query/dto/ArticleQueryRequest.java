package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleQueryRequest {

    private ArticleCategory category;   // 카테고리 필터
    private ArticleStatus status;       // 상태 필터
    private String sort;                // 정렬 (latest / popular)
    private Integer page;
    private Integer size;
}
