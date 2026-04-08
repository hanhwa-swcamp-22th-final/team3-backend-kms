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
    private Long historyId;

    private Long articleId;
    private Integer approvalVersion;
    private Long authorId;
    private Long approvedBy;
    private Long equipmentId;

    private String articleTitle;

    @Enumerated(EnumType.STRING)
    private ArticleCategory articleCategory;

    @Column(columnDefinition = "TEXT")
    private String articleContent;

    private LocalDateTime approvedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static KnowledgeEditHistory from(Long historyId, KnowledgeArticle article) {
        return KnowledgeEditHistory.builder()
                .historyId(historyId)
                .articleId(article.getArticleId())
                .approvalVersion(article.getApprovalVersion())
                .authorId(article.getAuthorId())
                .approvedBy(article.getApprovedBy())
                .equipmentId(article.getEquipmentId())
                .articleTitle(article.getArticleTitle())
                .articleCategory(article.getArticleCategory())
                .articleContent(article.getArticleContent())
                .approvedAt(article.getApprovedAt())
                .build();
    }
}
