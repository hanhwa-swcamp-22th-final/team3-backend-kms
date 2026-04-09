package com.ohgiraffers.team3backendkms.common.exception;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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
     * 발생 위치:
     *  - WorkerArticleController.register() → ArticleRegisterRequest 검증 실패
     *  - WorkerArticleController.draft() → ArticleDraftRequest 검증 실패
     *  - WorkerArticleController.update() → ArticleUpdateRequest 검증 실패
     *  - AdminArticleController.adminDelete() → ArticleAdminDeleteRequest 검증 실패
     *  - 기타 컨트롤러 @Valid 검증
     *
     * 반환:
     *  - HTTP Status: 400 Bad Request
     *  - errorCode: "VALIDATION_ERROR"
     *  - message: 필드별 검증 오류 메시지 (쉼표로 구분)
     *
     * 예시:
     * {
     *   "success": false,
     *   "errorCode": "VALIDATION_ERROR",
     *   "message": "제목은 5자 이상 200자 이하여야 합니다, 본문은 50자 이상이어야 합니다"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    /**
     * 핸들러 메서드 파라미터 검증 오류 처리 (@Positive 등 @PathVariable 검증)
     *
     * 발생 위치:
     *  - @PathVariable에 @Positive 등의 제약 조건 검증 실패
     *  - WorkerArticleController.update() → articleId 검증
     *  - WorkerArticleController.delete() → articleId 검증
     *  - AdminArticleController.adminDelete() → articleId 검증
     *  - AdminArticleController.adminUpdate() → articleId 검증
     *  - TeamLeaderArticleController.approve() → articleId 검증
     *  - TeamLeaderArticleController.reject() → articleId 검증
     *  - DepartmentLeaderArticleController.approve() → articleId 검증
     *  - DepartmentLeaderArticleController.reject() → articleId 검증
     *  - KnowledgeArticleQueryController.getArticleDetail() → articleId 검증
     *
     * 반환:
     *  - HTTP Status: 400 Bad Request
     *  - errorCode: "VALIDATION_ERROR"
     *  - message: 파라미터 검증 오류 메시지
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    /**
     * 비즈니스 예외 처리 (상태 검증, 입력값 검증, 리소스 미존재 등 모든 비즈니스 예외 통합 처리)
     *
     * HTTP 상태는 ArticleErrorCode에 정의된 값을 그대로 사용한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ArticleErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", "잘못된 요청 본문입니다."));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", e.getParameterName() + "는 필수입니다."));
    }

    /**
     * 예상하지 못한 모든 예외 처리 (마지막 보루)
     *
     * 발생 위치:
     *  - 모든 컨트롤러와 서비스에서 발생하는 예상 외 예외
     *  - 데이터베이스 오류
     *  - 시스템 오류
     *
     * 주의:
     *  - 이 핸들러로 오는 예외는 로그에 ERROR 레벨로 기록됨
     *  - 클라이언트에는 일반적인 오류 메시지만 반환 (보안)
     *  - 실제 예외 메시지는 서버 로그에서만 확인 가능
     *
     *  반환:
     *  - HTTP Status: 500 Internal Server Error
     *  - errorCode: "INTERNAL_ERROR"
     *  - message: "서버 오류가 발생했습니다." (고정 메시지)
     *
     *  예시:
     * {
     *   "success": false,
     *   "errorCode": "INTERNAL_ERROR",
     *   "message": "서버 오류가 발생했습니다.",
     *   "timestamp": "2026-04-01T15:30:00"
     * }
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        // 예상 외의 예외는 ERROR 레벨로 로깅
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}
