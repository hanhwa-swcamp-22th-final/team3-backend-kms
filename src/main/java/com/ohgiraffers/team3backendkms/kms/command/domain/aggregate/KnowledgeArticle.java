package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    private Long authorId;
    private Long approvedBy;
    private Long equipmentId;
    private Long fileGroupId;

    private String articleTitle;

    @Enumerated(EnumType.STRING)
    private ArticleCategory articleCategory;

    @Column(columnDefinition = "TEXT")
    private String articleContent;

    @Enumerated(EnumType.STRING)
    private ArticleStatus articleStatus;

    private String articleApprovalOpinion;
    private LocalDateTime approvedAt;

    private String articleRejectionReason;
    private String articleDeletionReason;

    private LocalDateTime deletedAt;
    private Boolean isDeleted;

    private Integer viewCount;

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    // =========================================================
    // 비즈니스 로직 메서드
    // =========================================================

    /* DRAFT → PENDING */
    public void submit() {
        if (this.articleStatus != ArticleStatus.DRAFT) {
            throw new IllegalStateException("[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다.");
        }
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* PENDING → APPROVED */
    public void approve(Long approvedBy, String opinion) {
        if (opinion != null && opinion.length() > 500) {
            throw new IllegalArgumentException("[APPROVAL_002] 승인 의견은 500자 이하여야 합니다.");
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException("[APPROVAL_003] PENDING 상태에서만 승인할 수 있습니다.");
        }
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.articleApprovalOpinion = opinion;
        this.approvedAt = LocalDateTime.now();
    }

    /* PENDING → REJECTED */
    public void reject(String reason) {
        if (reason == null || reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException("[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다.");
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException("[APPROVAL_003] PENDING 상태에서만 반려할 수 있습니다.");
        }
        this.articleStatus = ArticleStatus.REJECTED;
        this.articleRejectionReason = reason;
    }

    /* 소프트 딜리트 */
    public void softDelete() {
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException("[ARTICLE_008] 이미 삭제된 문서입니다.");
        }
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException("[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다.");
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
