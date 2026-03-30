package com.ohgiraffers.team3backendkms.auth.command.application.dto.request;

import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeRole;
import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.EmployeeTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRegisterRequest {

    private Long departmentId;
    private String employeeName;
    private String employeeEmail;
    private String employeePhone;
    private String employeeAddress;
    private String employeeEmergencyContact;
    private String employeePassword;
    private EmployeeRole employeeRole;
    private EmployeeTier employeeTier;
}
