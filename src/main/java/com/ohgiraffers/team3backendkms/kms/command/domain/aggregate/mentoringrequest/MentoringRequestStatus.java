package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest;

public enum MentoringRequestStatus {
    PENDING,   // 신청됨 — 담당 멘토 미확정, 수정 가능
    ACCEPTED,  // 수락됨 — mentor_id 확정, 수정 불가
    REJECTED   // 최종 종료 — 자동만료 또는 Admin 강제 종료
}
