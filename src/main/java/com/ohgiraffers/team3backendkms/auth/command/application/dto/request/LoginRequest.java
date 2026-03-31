package com.ohgiraffers.team3backendkms.auth.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private final String employeeEmail;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
    private final String password;

}
