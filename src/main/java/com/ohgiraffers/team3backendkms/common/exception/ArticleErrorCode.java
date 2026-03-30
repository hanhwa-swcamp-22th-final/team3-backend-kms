package com.ohgiraffers.team3backendkms.common.exception;

public enum ArticleErrorCode {

    // 문서 입력 검증
    ARTICLE_001("[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."),
    ARTICLE_002("[ARTICLE_002] 본문은 50자 이상이어야 합니다."),
    ARTICLE_003("[ARTICLE_003] 본문은 10,000자 이하여야 합니다."),

    // 문서 상태/권한
    ARTICLE_SUBMIT_INVALID("[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다."),
    ARTICLE_007("[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."),
    ARTICLE_008("[ARTICLE_008] 이미 삭제된 문서입니다."),
    ARTICLE_009("[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."),
    ARTICLE_NOT_FOUND("[ARTICLE] 문서를 찾을 수 없습니다."),

    // 승인/반려
    APPROVAL_001("[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."),
    APPROVAL_002("[APPROVAL_002] 승인 의견은 500자 이하여야 합니다."),
    APPROVAL_003("[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다.");

    private final String message;

    ArticleErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
