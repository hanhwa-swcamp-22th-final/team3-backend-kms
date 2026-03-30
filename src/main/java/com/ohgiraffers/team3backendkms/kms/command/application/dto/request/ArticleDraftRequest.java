package com.ohgiraffers.team3backendkms.kms.command.application.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDraftRequest {

    private Long authorId;          // 임시: JWT 연결 시 제거 예정
    private String title;           // nullable — 임시저장은 검증 없음
    private ArticleCategory category; // nullable
    private Long equipmentId;       // nullable
    private String content;         // nullable
}
