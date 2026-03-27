package com.ohgiraffers.team3backendkms.auth.command.application.service;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.LoginRequest;
import com.ohgiraffers.team3backendkms.auth.command.application.dto.response.TokenResponse;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Department;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Employee;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.RefreshToken;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.AuthRepository;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.DepartmentRepository;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.EmployeeRepository;
import com.ohgiraffers.team3backendkms.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository jpaAuthRepository;

    public TokenResponse login(LoginRequest loginRequest) {
        // 1. 이메일로 조회
        Employee employee = this.employeeRepository.findByEmployeeEmail(loginRequest.getEmployeeEmail())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다"));

        // 2. 비밀번호 매칭 확인
        if (!this.passwordEncoder.matches(loginRequest.getPassword(), employee.getEmployeePassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

        // 3. 부서 정보 조회
        Department department = this.departmentRepository.findById(employee.getDepartmentId())
                .orElseThrow(() -> new BadCredentialsException("부서 정보를 찾을 수 없습니다"));

        // 4. 토큰 생성
        String accessToken = this.jwtTokenProvider.createToken(
                employee.getEmployeeCode(), employee.getEmployeeRole().name(),
                employee.getEmployeeName(), department.getDepartmentName(), department.getTeamName());
        String refreshToken = this.jwtTokenProvider.createRefreshToken(
                employee.getEmployeeCode(), employee.getEmployeeRole().name(),
                employee.getEmployeeName(), department.getDepartmentName(), department.getTeamName());

        // 5. refresh token DB 저장
        RefreshToken tokenEntity = RefreshToken.builder()
                .employeeCode(employee.getEmployeeCode())
                .token(refreshToken)
                .expiryDate(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()))
                .build();

        this.jpaAuthRepository.save(tokenEntity);

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    /* DB refresh token 삭제 */
    public void logout(String refreshToken) {
        this.jwtTokenProvider.validateToken(refreshToken);
        String employeeCode = this.jwtTokenProvider.getEmployeeCodeFromJWT(refreshToken);
        this.jpaAuthRepository.deleteById(employeeCode);
    }

    /* refresh token 검증 후 새 token 발급 */
    public TokenResponse refreshToken(String provideRefreshToken) {
        this.jwtTokenProvider.validateToken(provideRefreshToken);

        String employeeCode = this.jwtTokenProvider.getEmployeeCodeFromJWT(provideRefreshToken);

        RefreshToken storedToken = this.jpaAuthRepository.findById(employeeCode)
                .orElseThrow(() -> new BadCredentialsException("해당 유저로 조회되는 refresh token 없음"));

        if (!storedToken.getToken().equals(provideRefreshToken)) {
            throw new BadCredentialsException("refresh token이 일치하지 않음");
        }

        if (storedToken.getExpiryDate().before(new Date())) {
            throw new BadCredentialsException("refresh token 기간 만료");
        }

        Employee employee = this.employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BadCredentialsException("해당 유저 없음"));

        Department department = this.departmentRepository.findById(employee.getDepartmentId())
                .orElseThrow(() -> new BadCredentialsException("부서 정보를 찾을 수 없습니다"));

        String accessToken = this.jwtTokenProvider.createToken(
                employee.getEmployeeCode(), employee.getEmployeeRole().name(),
                employee.getEmployeeName(), department.getDepartmentName(), department.getTeamName());
        String refreshToken = this.jwtTokenProvider.createRefreshToken(
                employee.getEmployeeCode(), employee.getEmployeeRole().name(),
                employee.getEmployeeName(), department.getDepartmentName(), department.getTeamName());

        RefreshToken tokenEntity = RefreshToken.builder()
                .employeeCode(employeeCode)
                .token(refreshToken)
                .expiryDate(new Date(System.currentTimeMillis() + this.jwtTokenProvider.getRefreshExpiration()))
                .build();

        this.jpaAuthRepository.save(tokenEntity);

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

}
