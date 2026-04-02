package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

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
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* PENDING → APPROVED (TL 또는 DL 승인) */
    public void approve(Long approverId, String opinion) {
        // 1️⃣ 삭제 여부 검증
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }

        // 2️⃣ 상태 검증
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_005.getMessage());
        }
        if (this.articleStatus == ArticleStatus.REJECTED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_006.getMessage());
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_003.getMessage());
        }

        // 3️⃣ 입력값 검증 (의견 길이)
        if (opinion != null && opinion.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.APPROVAL_002.getMessage());
        }

        // ✅ 모든 검증 통과 → 상태 변경
        this.articleStatus = ArticleStatus.APPROVED;
        this.approvedBy = approverId;
        this.articleApprovalOpinion = opinion;
        this.approvedAt = LocalDateTime.now();
    }

    /* PENDING → REJECTED (TL 또는 DL 반려) */
    public void reject(String reason) {
        // 1️⃣ 삭제 여부 검증
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }

        // 2️⃣ 상태 검증
        if (this.articleStatus == ArticleStatus.REJECTED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_007.getMessage());
        }
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_008.getMessage());
        }
        if (this.articleStatus != ArticleStatus.PENDING) {
            throw new IllegalStateException(ArticleErrorCode.APPROVAL_003.getMessage());
        }

        // 3️⃣ 입력값 검증 (반려 사유 길이)
        if (reason == null || reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.APPROVAL_001.getMessage());
        }

        // ✅ 모든 검증 통과 → 상태 변경
        this.articleStatus = ArticleStatus.REJECTED;
        this.articleRejectionReason = reason;
    }

    /* DRAFT → 필드 수정 후 PENDING 전환 */
    public void update(Long requesterId, String title, ArticleCategory category, String content) {
        // 1️⃣ 삭제 여부 검증
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }

        // 2️⃣ 상태 검증 (DRAFT만 수정 가능)
        if (this.articleStatus != ArticleStatus.DRAFT) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_006.getMessage());
        }

        // 3️⃣ 권한 검증 (본인만 수정 가능)
        if (!this.authorId.equals(requesterId)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
        }

        // 4️⃣ 입력값 검증
        if (title == null || title.length() < 5 || title.length() > 200) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_001.getMessage());
        }
        if (category == null) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_004.getMessage());
        }
        if (content == null || content.length() < 50) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_002.getMessage());
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_003.getMessage());
        }

        // ✅ 모든 검증 통과 → 필드 수정 후 PENDING 전환
        this.articleTitle = title;
        this.articleCategory = category;
        this.articleContent = content;
        this.articleStatus = ArticleStatus.PENDING;
    }

    /* 조회수 증가 */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /* 소프트 딜리트 (Worker — 본인이 삭제 가능, 특정 상태만 가능) */
    public void softDelete(Long requesterId) {
        // 1️⃣ 삭제 여부 검증
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }

        // 2️⃣ 상태 검증 (DRAFT, REJECTED만 삭제 가능)
        if (this.articleStatus == ArticleStatus.PENDING) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        }
        if (this.articleStatus == ArticleStatus.REJECTED) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        }
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_009.getMessage());
        }

        // 3️⃣ 권한 검증 (본인만 삭제 가능)
        if (!this.authorId.equals(requesterId)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
        }

        // ✅ 모든 검증 통과 → 소프트 삭제
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /* 관리자 삭제 — 모든 상태에서 삭제 가능 */
    public void adminDelete(String reason) {
        // 1️⃣ 삭제 여부 검증 (이미 삭제된 문서는 재삭제 불가)
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }

        // 2️⃣ 입력값 검증 (삭제 사유 길이)
        if (reason == null || reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_012.getMessage());
        }

        // ✅ 모든 검증 통과 → 관리자 삭제
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.articleDeletionReason = reason;
    }

    /* 관리자 수정 — 권한 검증 없이 수정 가능, 입력값만 검증 */
    public void adminUpdate(String title, ArticleCategory category, String content) {
        // 1️⃣ 입력값 검증
        if (title == null || title.length() < 5 || title.length() > 200) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_001.getMessage());
        }
        if (category == null) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_004.getMessage());
        }
        if (content == null || content.length() < 50) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_002.getMessage());
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_003.getMessage());
        }

        // ✅ 모든 검증 통과 → 필드 수정 (상태는 변경하지 않음)
        this.articleTitle = title;
        this.articleCategory = category;
        this.articleContent = content;
    }
}
