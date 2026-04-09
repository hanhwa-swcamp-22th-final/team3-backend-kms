package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle;

public enum ArticleCategory {
    TROUBLESHOOTING, // 장애조치
    PROCESS_IMPROVEMENT, // 공정개선
    EQUIPMENT_OPERATION, // 설비운영
    SAFETY, // 안전
    ETC; // 기타

    public String getDisplayName() {
        return switch (this) {
            case TROUBLESHOOTING -> "장애조치";
            case PROCESS_IMPROVEMENT -> "공정개선";
            case EQUIPMENT_OPERATION -> "설비운영";
            case SAFETY -> "안전";
            case ETC -> "기타";
        };
    }
}
