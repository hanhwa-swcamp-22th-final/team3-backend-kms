package com.ohgiraffers.team3backendkms.kms.query.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyArticleQueryRequest {

    private Long authorId;
    private ArticleStatus status;
    private Integer page;
    private Integer size;

    public int getOffset() {
        return (page != null && size != null) ? page * size : 0;
    }
}
