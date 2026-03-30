package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

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

    // 각 컬럼중 create.는 수정해도 업데이트 안한다고 (만약 덮어쓰기하면 setcreate같은 코드가잇었다)

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

    private static final String ERR_SUBMIT_NOT_DRAFT       = "[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다.";
    private static final String ERR_APPROVE_OPINION_LENGTH  = "[APPROVAL_002] 승인 의견은 500자 이하여야 합니다.";
    private static final String ERR_NOT_PENDING             = "[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다.";
    private static final String ERR_REJECT_REASON_LENGTH    = "[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다.";
    private static final String ERR_ALREADY_DELETED         = "[ARTICLE_008] 이미 삭제된 문서입니다.";
    private static final String ERR_DELETE_APPROVED         = "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다.";

    // =========================================================
    // 비즈니스 로직 메서드
    // =========================================================

    /* DRAFT → PENDING */
    public void submit() {
        if (this.articleStatus != ArticleStatus.DRAFT) {
            throw new IllegalStateException(ERR_SUBMIT_NOT_DRAFT);
        }
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* PENDING → APPROVED */
    public void approve(Long approvedBy, String opinion) {
        if (opinion != null && opinion.length() > 500) {
            throw new IllegalArgumentException(ERR_APPROVE_OPINION_LENGTH);
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException(ERR_NOT_PENDING);
        }
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.articleApprovalOpinion = opinion;
        this.approvedAt = LocalDateTime.now();
    }

    /* PENDING → REJECTED */
    public void reject(String reason) {
        if (reason == null || reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException(ERR_REJECT_REASON_LENGTH);
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException(ERR_NOT_PENDING);
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
            throw new IllegalStateException(ERR_ALREADY_DELETED);
        }
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ERR_DELETE_APPROVED);
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
