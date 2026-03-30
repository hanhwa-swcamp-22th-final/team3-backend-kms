package com.ohgiraffers.team3backendkms.auth.command.application.service;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.EmployeeRegisterRequest;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Employee;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeRole;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeStatus;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeTier;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.EmployeeRepository;
import com.ohgiraffers.team3backendkms.common.encryption.AesEncryptor;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService")
class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private IdGenerator idGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AesEncryptor aesEncryptor;

    private EmployeeRegisterRequest buildRequest() {
        return new EmployeeRegisterRequest(
                1L,
                "홍길동",
                "hong@company.com",
                "010-1234-5678",
                "서울시 강남구",
                "010-9999-0000",
                "rawPassword123!",
                EmployeeRole.WORKER,
                EmployeeTier.A
        );
    }

    @Nested
    @DisplayName("register()")
    class RegisterTest {

        @Test
        @DisplayName("정상 등록 시 직원 ID를 반환한다")
        void register_success() {
            // given
            EmployeeRegisterRequest request = buildRequest();
            given(aesEncryptor.encrypt("hong@company.com")).willReturn("enc-email");
            given(aesEncryptor.encrypt("010-1234-5678")).willReturn("enc-phone");
            given(aesEncryptor.encrypt("서울시 강남구")).willReturn("enc-address");
            given(aesEncryptor.encrypt("010-9999-0000")).willReturn("enc-emergency");
            given(employeeRepository.findByEmployeeEmail("enc-email")).willReturn(Optional.empty());
            given(idGenerator.generate()).willReturn(1000L);
            given(passwordEncoder.encode("rawPassword123!")).willReturn("$2a$10$hashed");

            Employee savedEmployee = Employee.builder()
                    .employeeId(1000L)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();
            given(employeeRepository.save(any(Employee.class))).willReturn(savedEmployee);

            // when
            Long result = employeeService.register(request);

            // then
            assertEquals(1000L, result);
        }

        @Test
        @DisplayName("저장 시 password는 BCrypt, 개인정보는 AES로 암호화된다")
        void register_encryptsFields() {
            // given
            EmployeeRegisterRequest request = buildRequest();
            given(aesEncryptor.encrypt("hong@company.com")).willReturn("enc-email");
            given(aesEncryptor.encrypt("010-1234-5678")).willReturn("enc-phone");
            given(aesEncryptor.encrypt("서울시 강남구")).willReturn("enc-address");
            given(aesEncryptor.encrypt("010-9999-0000")).willReturn("enc-emergency");
            given(employeeRepository.findByEmployeeEmail("enc-email")).willReturn(Optional.empty());
            given(idGenerator.generate()).willReturn(1000L);
            given(passwordEncoder.encode("rawPassword123!")).willReturn("$2a$10$hashed");

            Employee savedEmployee = Employee.builder().employeeId(1000L).build();
            given(employeeRepository.save(any(Employee.class))).willReturn(savedEmployee);

            // when
            employeeService.register(request);

            // then
            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            Employee saved = captor.getValue();
            assertEquals("enc-email",     saved.getEmployeeEmail());
            assertEquals("enc-phone",     saved.getEmployeePhone());
            assertEquals("enc-address",   saved.getEmployeeAddress());
            assertEquals("enc-emergency", saved.getEmployeeEmergencyContact());
            assertEquals("$2a$10$hashed", saved.getEmployeePassword());
            assertEquals(EmployeeStatus.ACTIVE, saved.getEmployeeStatus());
        }

        @Test
        @DisplayName("중복 이메일로 등록하면 예외가 발생한다")
        void register_duplicateEmail_throwsException() {
            // given
            EmployeeRegisterRequest request = buildRequest();
            given(aesEncryptor.encrypt("hong@company.com")).willReturn("enc-email");
            given(employeeRepository.findByEmployeeEmail("enc-email"))
                    .willReturn(Optional.of(Employee.builder().employeeId(99L).build()));

            // when & then
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> employeeService.register(request)
            );
            assertEquals("이미 등록된 이메일입니다.", ex.getMessage());
        }

        @Test
        @DisplayName("등록된 직원의 상태는 ACTIVE이다")
        void register_statusIsActive() {
            // given
            EmployeeRegisterRequest request = buildRequest();
            given(aesEncryptor.encrypt(anyString())).willReturn("encrypted");
            given(employeeRepository.findByEmployeeEmail("encrypted")).willReturn(Optional.empty());
            given(idGenerator.generate()).willReturn(2000L);
            given(passwordEncoder.encode(anyString())).willReturn("hashed");

            Employee savedEmployee = Employee.builder().employeeId(2000L).build();
            given(employeeRepository.save(any(Employee.class))).willReturn(savedEmployee);

            // when
            employeeService.register(request);

            // then
            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            assertEquals(EmployeeStatus.ACTIVE, captor.getValue().getEmployeeStatus());
            assertFalse(captor.getValue().getMfaEnabled());
            assertFalse(captor.getValue().getIsLocked());
            assertEquals(0, captor.getValue().getLoginFailCount());
        }
    }
}
