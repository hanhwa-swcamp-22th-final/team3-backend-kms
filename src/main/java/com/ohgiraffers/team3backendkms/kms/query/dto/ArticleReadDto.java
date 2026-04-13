package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ArticleReadDto {

    private Long articleId;
    private Long authorId;
    private String authorName;
    private String authorTier;
    private String articleTitle;
    private ArticleCategory articleCategory;
    private ArticleStatus articleStatus;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private Boolean bookmarked;
    private Boolean deleted;
}
