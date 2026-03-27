package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate;

public enum ArticleStatus {
    DRAFT , // 임시저장
    PENDING, // 승인 대기중
    APPROVED, // 승인된
    REJECTED // 반려된
}
