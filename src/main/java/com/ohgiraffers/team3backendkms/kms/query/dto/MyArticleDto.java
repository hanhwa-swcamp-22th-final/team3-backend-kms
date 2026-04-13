package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MyArticleDto {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private Long articleId;
    private String articleTitle;
    private ArticleCategory articleCategory;
    private ArticleStatus articleStatus;
    private Long equipmentId;
    private String equipmentName;
    private String articleContent;
    private String articleRejectionReason;
    private Integer viewCount;
    private Integer commentCount;
    private Integer reuseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KnowledgeTagReadDto> tags;

    public Long getId() {
        return articleId;
    }

    public String getTitle() {
        return articleTitle;
    }

    public String getCategory() {
        return articleCategory == null ? null : articleCategory.getDisplayName();
    }

    public String getStatus() {
        return articleStatus == null ? null : articleStatus.getDisplayName();
    }

    public String getEquipment() {
        return equipmentName;
    }

    public String getDate() {
        return createdAt == null ? null : createdAt.format(DATE_FORMATTER);
    }

    public String getSummary() {
        if (articleContent == null || articleContent.isBlank()) {
            return "";
        }
        String normalized = articleContent.replace("\r\n", "\n").replace('\n', ' ').trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "...";
    }

    public String getContent() {
        return articleContent;
    }

    public Integer getViews() {
        return viewCount == null ? 0 : viewCount;
    }

    public Integer getComments() {
        return commentCount == null ? 0 : commentCount;
    }

    public Integer getReuses() {
        return reuseCount == null ? 0 : reuseCount;
    }
}
