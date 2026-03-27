package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleReadDto {

    private Long articleId;
    private Long authorId;
    private String authorName;
    private String articleTitle;
    private ArticleCategory articleCategory;
    private ArticleStatus articleStatus;
    private Integer viewCount;
    private LocalDateTime createdAt;
}
