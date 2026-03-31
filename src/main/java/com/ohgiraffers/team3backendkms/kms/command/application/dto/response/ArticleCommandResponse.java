package com.ohgiraffers.team3backendkms.kms.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleCommandResponse {

    private Long articleId;
    private String message;

    public static ArticleCommandResponse of(Long articleId, String message) {
        return ArticleCommandResponse.builder()
                .articleId(articleId)
                .message(message)
                .build();
    }
}
