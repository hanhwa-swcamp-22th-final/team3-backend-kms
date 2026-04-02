package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* PENDING → APPROVED */
    public void approve(Long approverId, String reviewComment) {
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = reviewComment;
        this.approvedAt = LocalDateTime.now();
    }

    /* PENDING → REJECTED */
    public void reject(String reviewComment) {
        this.articleStatus = ArticleStatus.REJECTED;
        this.articleRejectionReason = reviewComment;
    }

    /* DRAFT → 필드 수정 후 PENDING 전환 */
    public void update(String title, ArticleCategory category, String content) {
        this.articleTitle = title;
        this.articleCategory = category;
        this.articleContent = content;
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* 조회수 증가 */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /* 소프트 딜리트 (Worker) */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /* 관리자 삭제 — 모든 상태에서 삭제 가능 */
    public void adminDelete(String reason) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.articleDeletionReason = reason;
    }

    /* 관리자 수정 — 상태 변경 없이 필드만 수정 */
    public void adminUpdate(String title, ArticleCategory category, String content) {
        this.articleTitle = title;
        this.articleCategory = category;
        this.articleContent = content;
    }
}
