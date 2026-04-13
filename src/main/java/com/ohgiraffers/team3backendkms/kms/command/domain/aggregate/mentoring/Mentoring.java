package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.MentoringErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentoring")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Mentoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mentoringId;

    @Column(nullable = false, unique = true)
    private Long requestId;   // mentoring_request.request_id 참조

    @Column(nullable = false)
    private Long mentorId;

    @Column(nullable = false)
    private Long menteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MentoringStatus mentoringStatus = MentoringStatus.IN_PROGRESS;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private Long updatedBy;

    // ── 비즈니스 메서드 ────────────────────────────────────────────

    public void complete(Long requesterId) {
        if (!this.mentorId.equals(requesterId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_030);
        }
        if (this.mentoringStatus != MentoringStatus.IN_PROGRESS) {
            throw new BusinessException(MentoringErrorCode.MENTORING_031);
        }
        this.mentoringStatus = MentoringStatus.COMPLETED;
    }
}
