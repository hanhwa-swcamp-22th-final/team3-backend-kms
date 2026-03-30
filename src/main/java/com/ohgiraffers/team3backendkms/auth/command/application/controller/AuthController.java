package com.ohgiraffers.team3backendkms.auth.command.application.controller;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.LoginRequest;
import com.ohgiraffers.team3backendkms.auth.command.application.dto.response.TokenResponse;
import com.ohgiraffers.team3backendkms.auth.command.application.service.AuthService;
import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = this.authService.login(loginRequest);
        return buildTokenResponse(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenResponse tokenResponse = this.authService.refreshToken(refreshToken);
        return buildTokenResponse(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken != null) {
            this.authService.logout(refreshToken);
        }

        ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.success(null));
    }

    private ResponseCookie createDeleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(TokenResponse tokenResponse) {
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(tokenResponse));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
    }

}
