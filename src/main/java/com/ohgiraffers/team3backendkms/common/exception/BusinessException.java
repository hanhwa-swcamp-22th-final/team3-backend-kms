package com.ohgiraffers.team3backendkms.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final KmsErrorCode errorCode;

    public BusinessException(KmsErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(KmsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
