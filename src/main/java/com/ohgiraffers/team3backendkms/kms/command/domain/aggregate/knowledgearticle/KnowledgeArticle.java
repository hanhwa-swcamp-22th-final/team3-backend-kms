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

    private Long originalArticleId;
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
    private Integer approvalVersion;

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
    // 비즈니스 로직 메서드
    // =========================================================

    /* DRAFT → PENDING */
    public void submit() {
        this.articleStatus = ArticleStatus.PENDING;
        this.approvedBy = null;
        this.articleApprovalOpinion = null;
        this.articleRejectionReason = null;
        this.approvedAt = null;
    }

    /* PENDING/REJECTED/DRAFT → DRAFT */
    public void saveAsDraft() {
        this.articleStatus = ArticleStatus.DRAFT;
    }

    /* PENDING → APPROVED */
    public void approve(Long approverId, String reviewComment) {
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = reviewComment;
        this.approvedAt = LocalDateTime.now();
        this.approvalVersion = (this.approvalVersion == null ? 0 : this.approvalVersion) + 1;
    }

    /* PENDING → REJECTED */
    public void reject(Long approverId, String reviewComment) {
        this.articleStatus = ArticleStatus.REJECTED;
        this.approvedBy = approverId;
        this.articleRejectionReason = reviewComment;
    }

    /* DRAFT → 필드 수정 후 DRAFT 유지 */
    public void updateDraft(String title, ArticleCategory category, Long equipmentId, String content) {
        this.articleTitle = title;
        this.articleCategory = category;
        this.equipmentId = equipmentId;
        this.articleContent = content;
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

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.articleDeletionReason = null;
    }

    /* PENDING 유지 — articleApprovalOpinion 저장 (임시저장) */
    public void hold(Long approverId, String reviewComment) {
        this.approvedBy = approverId;
        this.articleApprovalOpinion = reviewComment;
    }

    /* 관리자 수정 — 상태 변경 없이 필드만 수정 */
    public void adminUpdate(String title, ArticleCategory category, String content) {
        this.articleTitle = title;
        this.articleCategory = category;
        this.articleContent = content;
    }

    public boolean isRevisionCopy() {
        return this.originalArticleId != null;
    }

    public void applyApprovedRevision(KnowledgeArticle revision, Long approverId, String reviewComment) {
        this.equipmentId = revision.getEquipmentId();
        this.articleTitle = revision.getArticleTitle();
        this.articleCategory = revision.getArticleCategory();
        this.articleContent = revision.getArticleContent();
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = reviewComment;
        this.approvedAt = LocalDateTime.now();
        this.articleRejectionReason = null;
        this.approvalVersion = (this.approvalVersion == null ? 0 : this.approvalVersion) + 1;
    }
// 복사본생성-원본APPROVAL 상태유지
    public static KnowledgeArticle createRevisionCopy(Long articleId, KnowledgeArticle original) {
        return KnowledgeArticle.builder()
                .articleId(articleId)
                .originalArticleId(original.getArticleId())
                .authorId(original.getAuthorId())
                .approvedBy(null)
                .equipmentId(original.getEquipmentId())
                .fileGroupId(original.getFileGroupId())
                .articleTitle(original.getArticleTitle())
                .articleCategory(original.getArticleCategory())
                .articleContent(original.getArticleContent())
                .articleStatus(ArticleStatus.DRAFT)
                .articleApprovalOpinion(null)
                .approvedAt(null)
                .approvalVersion(original.getApprovalVersion())
                .articleRejectionReason(null)
                .articleDeletionReason(null)
                .deletedAt(null)
                .isDeleted(false)
                .viewCount(original.getViewCount())
                .build();
    }
}
