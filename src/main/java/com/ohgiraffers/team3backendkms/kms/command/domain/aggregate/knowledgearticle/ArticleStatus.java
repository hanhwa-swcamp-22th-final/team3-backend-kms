package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

public enum ArticleStatus {
    DRAFT,        // 임시저장
    PENDING,      // TL 검토 대기
    TL_APPROVED,  // TL 1차 승인 완료, DL 최종 승인 대기
    APPROVED,     // DL 최종 승인 완료
    REJECTED      // 반려됨
}
