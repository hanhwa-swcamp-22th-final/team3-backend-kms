package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MyArticleDto {

    private Long articleId;
    private String articleTitle;
    private ArticleCategory articleCategory;
    private ArticleStatus articleStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KnowledgeTagReadDto> tags;
}
