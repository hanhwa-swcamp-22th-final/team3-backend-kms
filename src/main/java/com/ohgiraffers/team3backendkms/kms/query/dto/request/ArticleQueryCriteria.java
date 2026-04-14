package com.ohgiraffers.team3backendkms.kms.query.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleQueryCriteria {

    private ArticleCategory category;
    private ArticleStatus status;
    private String sort;
    private String searchType;
    private String keyword;
    private Long articleIdKeyword;
    private Long requesterId;
    private String requesterRole;
    private Integer page;
    private Integer size;

    public int getOffset() {
        return (page != null && size != null) ? page * size : 0;
    }
}
