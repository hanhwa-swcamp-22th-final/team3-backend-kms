package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

public enum ArticleStatus {
    DRAFT,      // 임시저장
    PENDING,    // 승인 대기
    APPROVED,   // 승인 완료
    REJECTED    // 반려됨
}
