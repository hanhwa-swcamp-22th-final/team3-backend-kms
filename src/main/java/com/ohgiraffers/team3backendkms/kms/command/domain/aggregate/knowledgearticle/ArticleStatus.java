package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

public enum ArticleStatus {
    DRAFT,      // 임시저장
    PENDING,    // 승인 대기
    APPROVED,   // 승인 완료
    REJECTED;   // 반려됨

    public String getDisplayName() {
        return switch (this) {
            case DRAFT -> "임시 저장";
            case PENDING -> "승인 대기";
            case APPROVED -> "승인 완료";
            case REJECTED -> "반려";
        };
    }
}
