package com.ohgiraffers.team3backendkms.auth.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

}
