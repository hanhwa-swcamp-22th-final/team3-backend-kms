package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest;

import com.ohgiraffers.team3backendkms.common.converter.LongListJsonConverter;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentoring_request")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class MentoringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private Long articleId;   // 참조 문서 (선택)
    private Long mentorId;    // 수락 전 null, 수락 후 확정
    private Long menteeId;

    @Column(nullable = false, length = 100)
    private String mentoringField;   // ArticleCategory enum name

    @Column(nullable = false, length = 255)
    private String requestTitle;

    @Column(nullable = false, length = 1000)
    private String requestContent;

    private Integer mentoringDurationWeeks;

    @Column(length = 50)
    private String mentoringFrequency;

    @Enumerated(EnumType.STRING)
    private RequestPriority requestPriority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MentoringRequestStatus requestStatus = MentoringRequestStatus.PENDING;

    @Column(length = 500)
    private String rejectReason;

    @Convert(converter = LongListJsonConverter.class)
    @Column(columnDefinition = "JSON")
    @Builder.Default
    private List<Long> rejectedMentorIds = new ArrayList<>();

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

    private LocalDateTime processedAt;

    // ── 비즈니스 메서드 ────────────────────────────────────────────

    public void update(String title, String content, Integer durationWeeks, String frequency, RequestPriority priority) {
        if (this.requestStatus != MentoringRequestStatus.PENDING) {
            throw new BusinessException(MentoringErrorCode.MENTORING_013);
        }
        this.requestTitle = title;
        this.requestContent = content;
        this.mentoringDurationWeeks = durationWeeks;
        this.mentoringFrequency = frequency;
        this.requestPriority = priority;
    }

    public void accept(Long mentorId) {
        if (this.requestStatus != MentoringRequestStatus.PENDING) {
            throw new BusinessException(MentoringErrorCode.MENTORING_014);
        }
        this.mentorId = mentorId;
        this.requestStatus = MentoringRequestStatus.ACCEPTED;
        this.processedAt = LocalDateTime.now();
    }

    public void addRejectedMentor(Long mentorId) {
        if (this.rejectedMentorIds == null) {
            this.rejectedMentorIds = new ArrayList<>();
        }
        if (!this.rejectedMentorIds.contains(mentorId)) {
            this.rejectedMentorIds.add(mentorId);
        }
    }

    public boolean isRejectedBy(Long mentorId) {
        return this.rejectedMentorIds != null && this.rejectedMentorIds.contains(mentorId);
    }

    public void expireReject() {
        this.requestStatus = MentoringRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
}
