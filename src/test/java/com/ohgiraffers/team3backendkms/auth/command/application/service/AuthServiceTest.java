package com.ohgiraffers.team3backendkms.auth.command.application.service;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.LoginRequest;
import com.ohgiraffers.team3backendkms.auth.command.application.dto.response.TokenResponse;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.*;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.AuthRepository;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.DepartmentRepository;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.EmployeeRepository;
import com.ohgiraffers.team3backendkms.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRepository jpaAuthRepository;

    private Employee employee;
    private Department department;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeId(1L)
                .departmentId(1L)
                .employeeCode("EMP-0001")
                .employeeName("김관리")
                .employeeEmail("admin@company.com")
                .employeePassword("$2a$10$encodedPassword")
                .employeeRole(EmployeeRole.ADMIN)
                .employeeStatus(EmployeeStatus.ACTIVE)
                .build();

        department = Department.builder()
                .departmentId(1L)
                .parentDepartmentId(1L)
                .departmentName("경영지원본부")
                .teamName("시스템관리팀")
                .depth("Root")
                .build();
    }

    @Nested
    @DisplayName("login 메서드")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 토큰을 반환한다")
        void loginSuccess() {
            // given
            LoginRequest request = new LoginRequest("admin@company.com", "rawPassword");

            given(employeeRepository.findByEmployeeEmail("admin@company.com"))
                    .willReturn(Optional.of(employee));
            given(passwordEncoder.matches("rawPassword", "$2a$10$encodedPassword"))
                    .willReturn(true);
            given(departmentRepository.findById(1L))
                    .willReturn(Optional.of(department));
            given(jwtTokenProvider.createToken(
                    eq("EMP-0001"), eq("ADMIN"), eq("김관리"), eq("경영지원본부"), eq("시스템관리팀")))
                    .willReturn("access-token");
            given(jwtTokenProvider.createRefreshToken(
                    eq("EMP-0001"), eq("ADMIN"), eq("김관리"), eq("경영지원본부"), eq("시스템관리팀")))
                    .willReturn("refresh-token");
            given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);

            // when
            TokenResponse response = authService.login(request);

            // then
            assertNotNull(response);
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
            verify(jpaAuthRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void loginFailEmailNotFound() {
            // given
            LoginRequest request = new LoginRequest("unknown@company.com", "password");

            given(employeeRepository.findByEmployeeEmail("unknown@company.com"))
                    .willReturn(Optional.empty());

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );
            assertEquals("아이디 또는 비밀번호가 일치하지 않습니다", exception.getMessage());
        }

        @Test
        @DisplayName("비밀번호 불일치 시 예외가 발생한다")
        void loginFailPasswordMismatch() {
            // given
            LoginRequest request = new LoginRequest("admin@company.com", "wrongPassword");

            given(employeeRepository.findByEmployeeEmail("admin@company.com"))
                    .willReturn(Optional.of(employee));
            given(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword"))
                    .willReturn(false);

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );
            assertEquals("아이디 또는 비밀번호가 일치하지 않습니다", exception.getMessage());
        }

        @Test
        @DisplayName("부서 정보가 없을 경우 예외가 발생한다")
        void loginFailDepartmentNotFound() {
            // given
            LoginRequest request = new LoginRequest("admin@company.com", "rawPassword");

            given(employeeRepository.findByEmployeeEmail("admin@company.com"))
                    .willReturn(Optional.of(employee));
            given(passwordEncoder.matches("rawPassword", "$2a$10$encodedPassword"))
                    .willReturn(true);
            given(departmentRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );
            assertEquals("부서 정보를 찾을 수 없습니다", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("logout 메서드")
    class Logout {

        @Test
        @DisplayName("정상 로그아웃 시 refresh token을 삭제한다")
        void logoutSuccess() {
            // given
            String refreshToken = "valid-refresh-token";

            given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeCodeFromJWT(refreshToken)).willReturn("EMP-0001");

            // when
            authService.logout(refreshToken);

            // then
            verify(jpaAuthRepository).deleteById("EMP-0001");
        }
    }

    @Nested
    @DisplayName("refreshToken 메서드")
    class RefreshTokenTest {

        @Test
        @DisplayName("유효한 refresh token으로 새 토큰을 발급한다")
        void refreshTokenSuccess() {
            // given
            String provideRefreshToken = "valid-refresh-token";
            RefreshToken storedToken = RefreshToken.builder()
                    .employeeCode("EMP-0001")
                    .token("valid-refresh-token")
                    .expiryDate(new Date(System.currentTimeMillis() + 604800000L))
                    .build();

            given(jwtTokenProvider.validateToken(provideRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeCodeFromJWT(provideRefreshToken)).willReturn("EMP-0001");
            given(jpaAuthRepository.findById("EMP-0001")).willReturn(Optional.of(storedToken));
            given(employeeRepository.findByEmployeeCode("EMP-0001")).willReturn(Optional.of(employee));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(jwtTokenProvider.createToken(
                    eq("EMP-0001"), eq("ADMIN"), eq("김관리"), eq("경영지원본부"), eq("시스템관리팀")))
                    .willReturn("new-access-token");
            given(jwtTokenProvider.createRefreshToken(
                    eq("EMP-0001"), eq("ADMIN"), eq("김관리"), eq("경영지원본부"), eq("시스템관리팀")))
                    .willReturn("new-refresh-token");
            given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);

            // when
            TokenResponse response = authService.refreshToken(provideRefreshToken);

            // then
            assertNotNull(response);
            assertEquals("new-access-token", response.getAccessToken());
            assertEquals("new-refresh-token", response.getRefreshToken());
            verify(jpaAuthRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("DB에 저장된 refresh token이 없으면 예외가 발생한다")
        void refreshTokenNotFoundInDB() {
            // given
            String provideRefreshToken = "valid-refresh-token";

            given(jwtTokenProvider.validateToken(provideRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeCodeFromJWT(provideRefreshToken)).willReturn("EMP-0001");
            given(jpaAuthRepository.findById("EMP-0001")).willReturn(Optional.empty());

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.refreshToken(provideRefreshToken)
            );
            assertEquals("해당 유저로 조회되는 refresh token 없음", exception.getMessage());
        }

        @Test
        @DisplayName("refresh token이 불일치하면 예외가 발생한다")
        void refreshTokenMismatch() {
            // given
            String provideRefreshToken = "provided-token";
            RefreshToken storedToken = RefreshToken.builder()
                    .employeeCode("EMP-0001")
                    .token("different-stored-token")
                    .expiryDate(new Date(System.currentTimeMillis() + 604800000L))
                    .build();

            given(jwtTokenProvider.validateToken(provideRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeCodeFromJWT(provideRefreshToken)).willReturn("EMP-0001");
            given(jpaAuthRepository.findById("EMP-0001")).willReturn(Optional.of(storedToken));

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.refreshToken(provideRefreshToken)
            );
            assertEquals("refresh token이 일치하지 않음", exception.getMessage());
        }

        @Test
        @DisplayName("refresh token이 만료되었으면 예외가 발생한다")
        void refreshTokenExpired() {
            // given
            String provideRefreshToken = "expired-token";
            RefreshToken storedToken = RefreshToken.builder()
                    .employeeCode("EMP-0001")
                    .token("expired-token")
                    .expiryDate(new Date(System.currentTimeMillis() - 1000))
                    .build();

            given(jwtTokenProvider.validateToken(provideRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeCodeFromJWT(provideRefreshToken)).willReturn("EMP-0001");
            given(jpaAuthRepository.findById("EMP-0001")).willReturn(Optional.of(storedToken));

            // when & then
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.refreshToken(provideRefreshToken)
            );
            assertEquals("refresh token 기간 만료", exception.getMessage());
        }
    }
}
