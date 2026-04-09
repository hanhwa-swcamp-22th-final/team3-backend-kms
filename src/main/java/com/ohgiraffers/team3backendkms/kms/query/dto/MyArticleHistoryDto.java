package com.ohgiraffers.team3backendkms.kms.query.dto;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MyArticleHistoryDto {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private Long id;
    private String title;
    private LocalDateTime updatedAt;
    private ArticleStatus articleStatus;

    public String getDate() {
        return updatedAt == null ? null : updatedAt.format(DATE_FORMATTER);
    }

    public String getStatus() {
        return articleStatus == null ? null : articleStatus.getDisplayName();
    }
}
