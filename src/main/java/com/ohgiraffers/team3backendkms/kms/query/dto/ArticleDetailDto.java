package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ArticleDetailDto {

    private Long articleId;
    private Long authorId;
    private String authorName;
    private String articleTitle;
    private ArticleCategory articleCategory;
    private String articleContent;
    private ArticleStatus articleStatus;
    private String articleApprovalOpinion;
    private String articleRejectionReason;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
