package com.ohgiraffers.team3backendkms.common.exception;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException e) {
        return ApiResponse.failure("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalState(IllegalStateException e) {
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ApiResponse.failure("INTERNAL_ERROR", "서버 오류가 발생했습니다.");
    }
}
