package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgeedithistory;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
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

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "editor_id")
    private Long editorId;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "article_title")
    private String articleTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_category")
    private ArticleCategory articleCategory;

    @Column(name = "article_content", columnDefinition = "TEXT")
    private String articleContent;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static KnowledgeEditHistory from(Long historyId, KnowledgeArticle article, Long editorId) {
        return KnowledgeEditHistory.builder()
                .historyId(historyId)
                .articleId(article.getArticleId())
                .approvalVersion(article.getApprovalVersion())
                .authorId(article.getAuthorId())
                .editorId(editorId)
                .approvedBy(article.getApprovedBy())
                .equipmentId(article.getEquipmentId())
                .articleTitle(article.getArticleTitle())
                .articleCategory(article.getArticleCategory())
                .articleContent(article.getArticleContent())
                .approvedAt(article.getApprovedAt())
                .build();
    }
}
