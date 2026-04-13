package com.ohgiraffers.team3backendkms.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ArticleErrorCode.ARTICLE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(KmsErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(KmsErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
