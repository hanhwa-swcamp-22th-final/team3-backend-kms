package com.ohgiraffers.team3backendkms.infrastructure.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExternalApiResponse<T> {

    private Boolean success;
    private T data;
    private String errorCode;
    private String message;
    private String timestamp;
}
