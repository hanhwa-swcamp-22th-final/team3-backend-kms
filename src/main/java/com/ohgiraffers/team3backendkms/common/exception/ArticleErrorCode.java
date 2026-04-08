package com.ohgiraffers.team3backendkms.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * KMS 지식 문서 예외 코드 정의
 *
 * ✅ HTTP Status: 400 Bad Request (IllegalArgumentException, IllegalStateException)
 * ✅ HTTP Status: 404 Not Found (ResourceNotFoundException)
 *
 * 📝 사용 방법:
 * throw new IllegalArgumentException(ArticleErrorCode.ARTICLE_001.getMessage());
 * → GlobalExceptionHandler가 캐치하여 ApiResponse.failure("BAD_REQUEST", message) 반환
 */
@Getter
public enum ArticleErrorCode {

    // =====================================================
    // 📋 문서 입력값 검증 (register, update, draft)
    // =====================================================

    /**
     * 제목 길이 검증
     * 발생 위치: ArticleRegisterRequest, ArticleUpdateRequest, AdminArticleUpdateRequest
     * 호출 메서드: register(), update(), adminUpdate()
     * 조건: DTO Bean Validation 실패
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."
     */
    ARTICLE_001(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."),

    /**
     * 본문 최소 길이 검증
     * 발생 위치: ArticleRegisterRequest, ArticleUpdateRequest, AdminArticleUpdateRequest
     * 호출 메서드: register(), update(), adminUpdate()
     * 조건: DTO Bean Validation 실패
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_002] 본문은 50자 이상이어야 합니다."
     */
    ARTICLE_002(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_002] 본문은 50자 이상이어야 합니다."),

    /**
     * 본문 최대 길이 검증
     * 발생 위치: ArticleRegisterRequest, ArticleUpdateRequest, AdminArticleUpdateRequest
     * 호출 메서드: register(), update(), adminUpdate()
     * 조건: DTO Bean Validation 실패
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_003] 본문은 10,000자 이하여야 합니다."
     */
    ARTICLE_003(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_003] 본문은 10,000자 이하여야 합니다."),

    /**
     * 카테고리 필수 검증
     * 발생 위치: ArticleRegisterRequest, ArticleUpdateRequest, AdminArticleUpdateRequest
     * 호출 메서드: register(), update(), adminUpdate()
     * 조건: DTO Bean Validation 실패
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_004] 카테고리는 필수입니다."
     */
    ARTICLE_004(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_004] 카테고리는 필수입니다."),

    /**
     * 설비 ID 필수 및 유효성 검증
     * 발생 위치: KnowledgeArticleCommandService.validateEquipmentId(), validateEquipmentIdIfPresent()
     * 호출 메서드: register(), draft()
     * 조건: equipmentId == null OR equipmentId <= 0, 또는 제공된 equipmentId <= 0
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_005] 유효하지 않은 설비 ID입니다."
     */
    ARTICLE_005(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_005] 유효하지 않은 설비 ID입니다."),

    // =====================================================================
    // 📋 문서 상태/권한 검증 (submit, update, delete, softDelete, etc)
    // =====================================================================

    /**
     * DRAFT 상태가 아닌 문서 제출 시도
     * 발생 위치: KnowledgeArticle.submit()
     * 호출 메서드: (내부에서만 사용)
     * 조건: articleStatus != ArticleStatus.DRAFT
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다."
     */
    ARTICLE_SUBMIT_INVALID(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다."),

    /**
     * DRAFT 상태가 아닌 문서 수정 시도
     * 발생 위치: KnowledgeArticle.update()
     * 호출 메서드: KnowledgeArticleCommandService.update(), adminUpdate()
     * 조건: articleStatus != ArticleStatus.DRAFT
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_006] DRAFT 상태에서만 수정할 수 있습니다."
     */
    ARTICLE_006(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_006] DRAFT 상태에서만 수정할 수 있습니다."),

    /**
     * 본인이 아닌 문서 삭제 시도 (권한 검증)
     * 발생 위치: KnowledgeArticleCommandService.delete()
     * 호출 메서드: DELETE /api/kms/articles/{articleId}
     * 조건: article.authorId != requesterId
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."
     */
    ARTICLE_007(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."),

    /**
     * 이미 삭제된 문서 접근 (중복 삭제 또는 삭제된 문서 조회)
     * 발생 위치: KnowledgeArticle.softDelete(), update(), approve(), reject(), etc
     * 호출 메서드: 모든 문서 수정 작업
     * 조건: isDeleted == true
     * HTTP 응답:
     *   - Status: 400 또는 404
     *   - errorCode: "BAD_REQUEST" 또는 "NOT_FOUND"
     *   - message: "[ARTICLE_008] 이미 삭제된 문서입니다."
     */
    ARTICLE_008(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_008] 이미 삭제된 문서입니다."),

    /**
     * 승인 완료 문서의 직접 삭제 시도
     * 발생 위치: KnowledgeArticle.softDelete()
     * 호출 메서드: DELETE /api/kms/articles/{articleId}
     * 조건: articleStatus == ArticleStatus.APPROVED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
     * 📌 관리자만 DELETE /api/kms/admin/articles/{articleId}로 삭제 가능
     */
    ARTICLE_009(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."),

    /**
     * 평가 진행 중인 문서 삭제 시도 (PENDING 또는 REJECTED)
     * 발생 위치: KnowledgeArticle.softDelete()
     * 호출 메서드: DELETE /api/kms/articles/{articleId}
     * 조건: articleStatus == ArticleStatus.PENDING OR articleStatus == ArticleStatus.REJECTED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."
     * 📌 PENDING: 첫 제출 후 승인 대기 중
     * 📌 REJECTED: 반려된 상태
     */
    ARTICLE_010(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."),

    /**
     * 승인 완료 문서가 아닌 문서에서 수정 시작 시도
     */
    ARTICLE_011(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_011] 승인 완료된 문서에서만 수정 시작할 수 있습니다."),

    /**
     * 삭제 사유 길이 검증 (관리자 삭제 전용)
     * 발생 위치: KnowledgeArticle.adminDelete()
     * 호출 메서드: DELETE /api/kms/admin/articles/{articleId}
     * 조건: deletionReason == null OR deletionReason.length() < 10 OR deletionReason.length() > 500
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[ARTICLE_012] 삭제 사유는 10자 이상 500자 이하여야 합니다."
     */
    ARTICLE_012(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[ARTICLE_012] 삭제 사유는 10자 이상 500자 이하여야 합니다."),

    /**
     * 문서 미존재 (리소스 찾을 수 없음)
     * 발생 위치: KnowledgeArticleCommandService.findArticleById(),
     *           KnowledgeArticleApprovalService.findArticleById()
     * 호출 메서드: 모든 문서 조회/수정 작업
     * 조건: knowledgeArticleRepository.findById(articleId).isEmpty()
     * HTTP 응답:
     *   - Status: 404
     *   - errorCode: "NOT_FOUND"
     *   - message: "[ARTICLE] 문서를 찾을 수 없습니다."
     */
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "[ARTICLE] 문서를 찾을 수 없습니다."),

    // ==========================================================
    // 📋 승인/반려 검증 (approve, reject)
    // ==========================================================

    /**
     * 반려 사유 길이 검증
     * 발생 위치: KnowledgeArticle.reject()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/reject
     *            POST /api/kms/dl/approval/{articleId}/reject
     * 조건: reviewComment == null OR reviewComment.length() < 10 OR reviewComment.length() > 500
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."
     */
    APPROVAL_001(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."),

    /**
     * 승인 의견 길이 검증
     * 발생 위치: KnowledgeArticle.approve()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/approve
     *            POST /api/kms/dl/approval/{articleId}/approve
     * 조건: reviewComment != null AND reviewComment.length() > 500
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_002] 승인 의견은 500자 이하여야 합니다."
     */
    APPROVAL_002(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_002] 승인 의견은 500자 이하여야 합니다."),

    /**
     * PENDING 상태가 아닌 문서에 대한 승인/반려 시도
     * 발생 위치: KnowledgeArticle.approve(), reject()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/{approve|reject}
     *            POST /api/kms/dl/approval/{articleId}/{approve|reject}
     * 조건: articleStatus != ArticleStatus.PENDING
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."
     */
    APPROVAL_003(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."),

    /**
     * 이미 승인된 문서의 재승인 시도
     * 발생 위치: KnowledgeArticle.approve()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/approve
     *            POST /api/kms/dl/approval/{articleId}/approve
     * 조건: articleStatus == ArticleStatus.APPROVED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_005] 이미 승인된 문서입니다."
     */
    APPROVAL_005(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_005] 이미 승인된 문서입니다."),

    /**
     * 반려된 문서의 승인 시도
     * 발생 위치: KnowledgeArticle.approve()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/approve
     *            POST /api/kms/dl/approval/{articleId}/approve
     * 조건: articleStatus == ArticleStatus.REJECTED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_006] 반려된 문서는 승인할 수 없습니다."
     */
    APPROVAL_006(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_006] 반려된 문서는 승인할 수 없습니다."),

    /**
     * 이미 반려된 문서의 재반려 시도
     * 발생 위치: KnowledgeArticle.reject()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/reject
     *            POST /api/kms/dl/approval/{articleId}/reject
     * 조건: articleStatus == ArticleStatus.REJECTED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_007] 이미 반려된 문서입니다."
     */
    APPROVAL_007(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_007] 이미 반려된 문서입니다."),

    /**
     * 승인 완료 문서의 반려 시도
     * 발생 위치: KnowledgeArticle.reject()
     * 호출 메서드: POST /api/kms/tl/approval/{articleId}/reject
     *            POST /api/kms/dl/approval/{articleId}/reject
     * 조건: articleStatus == ArticleStatus.APPROVED
     * HTTP 응답:
     *   - Status: 400
     *   - errorCode: "BAD_REQUEST"
     *   - message: "[APPROVAL_008] 승인 완료된 문서는 반려할 수 없습니다."
     */
    APPROVAL_008(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[APPROVAL_008] 승인 완료된 문서는 반려할 수 없습니다."),

    // ==========================================================
    // 태그 검증 (tag create, update, delete)
    // ==========================================================

    TAG_001(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[TAG_001] 이미 존재하는 태그 이름입니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "[TAG] 태그를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ArticleErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
