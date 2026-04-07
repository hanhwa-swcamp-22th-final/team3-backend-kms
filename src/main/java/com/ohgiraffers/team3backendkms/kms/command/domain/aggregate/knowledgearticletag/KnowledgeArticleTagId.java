package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Table(name = "knowledge_article_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KnowledgeArticleTagId implements Serializable {

    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "article_id")
    private Long articleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeArticleTagId that = (KnowledgeArticleTagId) o;
        return Objects.equals(tagId, that.tagId) && Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, articleId);
    }
}
