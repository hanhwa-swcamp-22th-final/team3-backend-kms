package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "mentoring_request")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MentoringRequest {

    @Id
    private Long requestId;
    private Long menteeId;
    private Long mentorId;
    private Long articleId;
    private String mentoringField;
    private String requestTitle;
    private String requestContent;
    private Integer mentoringDurationWeeks;
    private String mentoringFrequency;

    @Enumerated(EnumType.STRING)
    private RequestPriority requestPriority;

    @Enumerated(EnumType.STRING)
    private MentoringRequestStatus requestStatus;

    private String rejectReason;

    @Column(columnDefinition = "json")
    private String rejectedMentorIds;

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
}
