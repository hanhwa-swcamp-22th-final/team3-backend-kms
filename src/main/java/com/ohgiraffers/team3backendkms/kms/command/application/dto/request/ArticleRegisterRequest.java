package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleRegisterRequest {

    private Long authorId;          // 임시: JWT 연결 시 제거 예정
    private String title;
    private ArticleCategory category;
    private Long equipmentId;       // nullable
    private String content;
}
