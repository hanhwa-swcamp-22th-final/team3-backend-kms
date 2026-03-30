package com.ohgiraffers.team3backendkms.auth.command.application.service;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.EmployeeRegisterRequest;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Employee;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeStatus;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.EmployeeRepository;
import com.ohgiraffers.team3backendkms.common.encryption.AesEncryptor;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AesEncryptor aesEncryptor;

    /* 직원 등록
     * - password  : BCrypt 단방향 암호화
     * - email, phone, address, emergencyContact : AES-256 양방향 암호화
     */
    public Long register(EmployeeRegisterRequest request) {
        String encryptedEmail = aesEncryptor.encrypt(request.getEmployeeEmail());

        if (employeeRepository.findByEmployeeEmail(encryptedEmail).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        Long employeeId = idGenerator.generate();
        String employeeCode = "EMP-" + String.format("%06d", employeeId % 1_000_000);

        Employee employee = Employee.builder()
                .employeeId(employeeId)
                .departmentId(request.getDepartmentId())
                .employeeCode(employeeCode)
                .employeeName(request.getEmployeeName())
                .employeeEmail(encryptedEmail)                                         // AES
                .employeePhone(aesEncryptor.encrypt(request.getEmployeePhone()))       // AES
                .employeeAddress(aesEncryptor.encrypt(request.getEmployeeAddress()))   // AES
                .employeeEmergencyContact(aesEncryptor.encrypt(request.getEmployeeEmergencyContact())) // AES
                .employeePassword(passwordEncoder.encode(request.getEmployeePassword())) // BCrypt
                .employeeRole(request.getEmployeeRole())
                .employeeTier(request.getEmployeeTier())
                .employeeStatus(EmployeeStatus.ACTIVE)
                .mfaEnabled(false)
                .loginFailCount(0)
                .isLocked(false)
                .build();

        return employeeRepository.save(employee).getEmployeeId();
    }
}
