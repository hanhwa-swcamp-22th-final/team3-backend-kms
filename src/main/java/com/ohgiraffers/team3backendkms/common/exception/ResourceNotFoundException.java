package com.ohgiraffers.team3backendkms.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ArticleErrorCode.ARTICLE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(ArticleErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ArticleErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
