package com.ohgiraffers.team3backendkms.kms.query.dto.mentoring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MentoringRequestListDto {
    private Long requestId;
    private Long menteeId;
    private String menteeName;
    private String mentoringField;
    private String requestTitle;
    private String requestContent;
    private String requestStatus;    // MentoringRequestStatus name
    private LocalDateTime requestedAt;
}
