package com.ohgiraffers.team3backendkms.common.exception;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 *
 * 모든 Controller에서 발생하는 예외를 중앙에서 처리하여 일관된 응답 형식 제공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO 검증 오류 처리
     *
     * 🔴 발생 위치:
     *  - WorkerArticleController.register() → ArticleRegisterRequest 검증 실패
     *  - WorkerArticleController.draft() → ArticleDraftRequest 검증 실패
     *  - WorkerArticleController.update() → ArticleUpdateRequest 검증 실패
     *  - AdminArticleController.adminDelete() → ArticleAdminDeleteRequest 검증 실패
     *  - 기타 컨트롤러 @Valid 검증
     *
     * ✅ 반환:
     *  - HTTP Status: 400 Bad Request
     *  - errorCode: "VALIDATION_ERROR"
     *  - message: 필드별 검증 오류 메시지 (쉼표로 구분)
     *
     * 📝 예시:
     * {
     *   "success": false,
     *   "errorCode": "VALIDATION_ERROR",
     *   "message": "제목은 5자 이상 200자 이하여야 합니다, 본문은 50자 이상이어야 합니다"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.failure("VALIDATION_ERROR", message);
    }

    /**
     * 리소스 미찾음 처리
     *
     * 🔴 발생 위치:
     *  - KnowledgeArticleService.delete() → 문서 미존재
     *  - KnowledgeArticleService.update() → 문서 미존재
     *  - KnowledgeArticleService.approve() → 문서 미존재
     *  - KnowledgeArticleService.reject() → 문서 미존재
     *  - KnowledgeArticleService.adminDelete() → 문서 미존재
     *  - KnowledgeArticleService.incrementViewCount() → 문서 미존재
     *
     * 💥 에러 코드:
     *  - ARTICLE_NOT_FOUND: "[ARTICLE] 문서를 찾을 수 없습니다."
     *
     * ✅ 반환:
     *  - HTTP Status: 404 Not Found
     *  - errorCode: "NOT_FOUND"
     *  - message: "[ARTICLE] 문서를 찾을 수 없습니다."
     *
     * 📝 예시:
     * {
     *   "success": false,
     *   "errorCode": "NOT_FOUND",
     *   "message": "[ARTICLE] 문서를 찾을 수 없습니다."
     * }
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException e) {
        return ApiResponse.failure("NOT_FOUND", e.getMessage());
    }

    /**
     * 비즈니스 로직 검증 오류 처리 (입력값 검증)
     *
     * 🔴 발생 위치:
     *  - KnowledgeArticleService.register() → 제목, 본문, 카테고리 검증
     *  - KnowledgeArticleService.update() → 제목, 본문, 카테고리 검증
     *  - KnowledgeArticleService.adminUpdate() → 제목, 본문, 카테고리 검증
     *  - KnowledgeArticle.approve() → 의견(opinion) 길이 검증
     *  - KnowledgeArticle.reject() → 반려 사유(reason) 길이 검증
     *  - KnowledgeArticle.adminDelete() → 삭제 사유(reason) 길이 검증
     *
     * 💥 에러 코드 (service에서 검증):
     *  - ARTICLE_001: "[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."
     *  - ARTICLE_002: "[ARTICLE_002] 본문은 50자 이상이어야 합니다."
     *  - ARTICLE_003: "[ARTICLE_003] 본문은 10,000자 이하여야 합니다."
     *  - ARTICLE_004: "[ARTICLE_004] 카테고리는 필수입니다."
     *  - ARTICLE_012: "[ARTICLE_012] 삭제 사유는 10자 이상 500자 이하여야 합니다."
     *
     * 💥 에러 코드 (entity에서 검증):
     *  - APPROVAL_001: "[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."
     *  - APPROVAL_002: "[APPROVAL_002] 승인 의견은 500자 이하여야 합니다."
     *
     * ✅ 반환:
     *  - HTTP Status: 400 Bad Request
     *  - errorCode: "BAD_REQUEST"
     *  - message: "[ARTICLE_XXX] 또는 [APPROVAL_XXX] 에러 메시지"
     *
     * 📝 예시 1 (제목 검증 실패):
     * {
     *   "success": false,
     *   "errorCode": "BAD_REQUEST",
     *   "message": "[ARTICLE_001] 제목은 5자 이상 200자 이하여야 합니다."
     * }
     *
     * 📝 예시 2 (반려 사유 검증 실패):
     * {
     *   "success": false,
     *   "errorCode": "BAD_REQUEST",
     *   "message": "[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."
     * }
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }

    /**
     * 비즈니스 로직 상태 검증 오류 처리
     *
     * 🔴 발생 위치:
     *  - KnowledgeArticle.submit() → DRAFT 아닌 상태에서 제출
     *  - KnowledgeArticle.approve() → 상태 검증 실패
     *  - KnowledgeArticle.reject() → 상태 검증 실패
     *  - KnowledgeArticle.update() → DRAFT 상태 검증 실패
     *  - KnowledgeArticle.softDelete() → 삭제 불가 상태
     *  - KnowledgeArticle.adminDelete() → 삭제 실패
     *  - KnowledgeArticleService.delete() → 권한 검증 실패
     *
     * 💥 에러 코드 (submit):
     *  - ARTICLE_SUBMIT_INVALID: "[ARTICLE] DRAFT 상태에서만 제출할 수 있습니다."
     *
     * 💥 에러 코드 (approve):
     *  - APPROVAL_005: "[APPROVAL_005] 이미 승인된 문서입니다."
     *  - APPROVAL_006: "[APPROVAL_006] 반려된 문서는 승인할 수 없습니다."
     *  - APPROVAL_003: "[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."
     *
     * 💥 에러 코드 (reject):
     *  - APPROVAL_007: "[APPROVAL_007] 이미 반려된 문서입니다."
     *  - APPROVAL_008: "[APPROVAL_008] 승인 완료된 문서는 반려할 수 없습니다."
     *  - APPROVAL_003: "[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."
     *
     * 💥 에러 코드 (update):
     *  - ARTICLE_006: "[ARTICLE_006] DRAFT 상태에서만 수정할 수 있습니다."
     *  - ARTICLE_008: "[ARTICLE_008] 이미 삭제된 문서입니다."
     *
     * 💥 에러 코드 (softDelete):
     *  - ARTICLE_008: "[ARTICLE_008] 이미 삭제된 문서입니다."
     *  - ARTICLE_010: "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."
     *  - ARTICLE_009: "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
     *
     * 💥 에러 코드 (delete - 권한):
     *  - ARTICLE_007: "[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."
     *
     * ✅ 반환:
     *  - HTTP Status: 400 Bad Request
     *  - errorCode: "BAD_REQUEST"
     *  - message: "[ARTICLE/APPROVAL_XXX] 에러 메시지"
     *
     * 📝 예시 1 (APPROVED 상태 삭제 시도):
     * {
     *   "success": false,
     *   "errorCode": "BAD_REQUEST",
     *   "message": "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
     * }
     *
     * 📝 예시 2 (이미 승인된 문서 승인 시도):
     * {
     *   "success": false,
     *   "errorCode": "BAD_REQUEST",
     *   "message": "[APPROVAL_005] 이미 승인된 문서입니다."
     * }
     *
     * 📝 예시 3 (본인이 아닌 문서 삭제 시도):
     * {
     *   "success": false,
     *   "errorCode": "BAD_REQUEST",
     *   "message": "[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."
     * }
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalState(IllegalStateException e) {
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }

    /**
     * 예상하지 못한 모든 예외 처리 (마지막 보루)
     *
     * 🔴 발생 위치:
     *  - 모든 컨트롤러와 서비스에서 발생하는 예상 외 예외
     *  - 데이터베이스 오류
     *  - 시스템 오류
     *
     * ⚠️ 주의:
     *  - 이 핸들러로 오는 예외는 로그에 ERROR 레벨로 기록됨
     *  - 클라이언트에는 일반적인 오류 메시지만 반환 (보안)
     *  - 실제 예외 메시지는 서버 로그에서만 확인 가능
     *
     * ✅ 반환:
     *  - HTTP Status: 500 Internal Server Error
     *  - errorCode: "INTERNAL_ERROR"
     *  - message: "서버 오류가 발생했습니다." (고정 메시지)
     *
     * 📝 예시:
     * {
     *   "success": false,
     *   "errorCode": "INTERNAL_ERROR",
     *   "message": "서버 오류가 발생했습니다.",
     *   "timestamp": "2026-04-01T15:30:00"
     * }
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneralException(Exception e) {
        // 예상 외의 예외는 ERROR 레벨로 로깅
        log.error("예상치 못한 오류 발생", e);
        return ApiResponse.failure("INTERNAL_ERROR", "서버 오류가 발생했습니다.");
    }
}
