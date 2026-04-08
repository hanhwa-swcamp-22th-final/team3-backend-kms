package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgeedithistory;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "knowledge_edit_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_knowledge_edit_history_article_version", columnNames = {"article_id", "approval_version"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KnowledgeEditHistory {

    @Id
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "approval_version")
    private Integer approvalVersion;

    @Column(name = "article_title")
    private String articleTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_category")
    private ArticleCategory articleCategory;

    @Column(name = "article_previous_content", columnDefinition = "TEXT")
    private String articlePreviousContent;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    public static KnowledgeEditHistory from(Long historyId, KnowledgeArticle article) {
        return KnowledgeEditHistory.builder()
                .historyId(historyId)
                .articleId(article.getArticleId())
                .approvalVersion(article.getApprovalVersion())
                .articleTitle(article.getArticleTitle())
                .articleCategory(article.getArticleCategory())
                .articlePreviousContent(article.getArticleContent())
                .editedAt(LocalDateTime.now())
                .build();
    }
}
