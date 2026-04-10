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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void accept(Long mentorId) {
        this.mentorId = mentorId;
        this.requestStatus = MentoringRequestStatus.ACCEPTED;
        this.rejectReason = null;
    }

    public void rejectByMentor(Long mentorId) {
        Set<String> rejectedIds = parseRejectedMentorIds();
        rejectedIds.add(String.valueOf(mentorId));
        this.rejectedMentorIds = rejectedIds.stream()
                .collect(Collectors.joining(",", "[", "]"));
    }

    public boolean hasRejectedMentor(Long mentorId) {
        return parseRejectedMentorIds().contains(String.valueOf(mentorId));
    }

    private Set<String> parseRejectedMentorIds() {
        if (rejectedMentorIds == null || rejectedMentorIds.isBlank()) {
            return new LinkedHashSet<>();
        }

        String normalized = rejectedMentorIds
                .replace("[", "")
                .replace("]", "")
                .trim();

        if (normalized.isBlank()) {
            return new LinkedHashSet<>();
        }

        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
