package com.ohgiraffers.team3backendkms.common.exception;

import org.springframework.http.HttpStatus;

public interface KmsErrorCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
