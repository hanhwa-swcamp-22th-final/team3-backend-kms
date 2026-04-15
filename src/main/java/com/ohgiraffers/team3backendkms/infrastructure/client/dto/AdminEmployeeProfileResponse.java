package com.ohgiraffers.team3backendkms.infrastructure.client.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminEmployeeProfileResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String currentTier;
    private BigDecimal totalScore;
}
