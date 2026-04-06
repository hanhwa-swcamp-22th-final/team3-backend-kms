package com.ohgiraffers.team3backendkms.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ArticleErrorCode errorCode;

    public BusinessException(ArticleErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ArticleErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
