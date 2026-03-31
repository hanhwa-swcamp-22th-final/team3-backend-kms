package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ohgiraffers.team3backendkms.common.exception.ArticleErrorCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_article")
@EntityListeners(AuditingEntityListener.class) // 이벤트리스너로 감지
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class KnowledgeArticle {

    @Id
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

    // @CreatedDate, @CreatedBy는 updatable=false — INSERT 시에만 세팅되고 UPDATE 시 변경되지 않음

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

    // =========================================================
    // 예외 메시지 상수
    // =========================================================


    // =========================================================
    // 비즈니스 로직 메서드
    // =========================================================

    /* DRAFT → PENDING */
    public void submit() {
        if (this.articleStatus != ArticleStatus.DRAFT) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_SUBMIT_INVALID.getMessage());
        }
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* PENDING → TL_APPROVED (TL 1차 승인) */
    public void tlApprove(Long approverId, String opinion) {
        if (opinion != null && opinion.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.APPROVAL_002.getMessage());
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_003.getMessage());
        }
        this.articleStatus = ArticleStatus.TL_APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = opinion;
    }

    /* TL_APPROVED → APPROVED (DL 최종 승인) */
    public void approve(Long approverId, String opinion) {
        if (opinion != null && opinion.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.APPROVAL_002.getMessage());
        }
        if (this.articleStatus != ArticleStatus.TL_APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_004.getMessage());
        }
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = opinion;
        this.approvedAt = LocalDateTime.now();
    }

    /* PENDING 또는 TL_APPROVED → REJECTED (TL/DL 반려) */
    public void reject(String reason) {
        if (reason == null || reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.APPROVAL_001.getMessage());
        }
        if (this.articleStatus != ArticleStatus.PENDING && this.articleStatus != ArticleStatus.TL_APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_003.getMessage());
        }
        this.articleStatus = ArticleStatus.REJECTED;
        this.articleRejectionReason = reason;
    }

    /* 조회수 증가 */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /* 소프트 딜리트 */
    public void softDelete() {
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_009.getMessage());
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
