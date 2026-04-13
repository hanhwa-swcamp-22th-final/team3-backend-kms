package com.ohgiraffers.team3backendkms.kms.query.dto.mentoring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MentoringListDto {
    private Long mentoringId;
    private Long requestId;
    private Long mentorId;
    private String mentorName;
    private Long menteeId;
    private String menteeName;
    private String mentoringField;
    private String mentoringStatus;  // MentoringStatus name
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
