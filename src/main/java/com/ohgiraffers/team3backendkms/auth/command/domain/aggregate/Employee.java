package com.ohgiraffers.team3backendkms.auth.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "employee_email")
    private String employeeEmail;

    @Column(name = "employee_phone")
    private String employeePhone;

    @Column(name = "employee_address")
    private String employeeAddress;

    @Column(name = "employee_emergency_contact", length = 30)
    private String employeeEmergencyContact;

    @Column(name = "employee_password")
    private String employeePassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role")
    private EmployeeRole employeeRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_status")
    private EmployeeStatus employeeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_tier")
    private EmployeeTier employeeTier;

    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled;

    @Column(name = "login_fail_count")
    private Integer loginFailCount;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
