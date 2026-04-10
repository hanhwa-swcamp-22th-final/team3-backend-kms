package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentoringEmployee {

    @Id
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    private MentoringEmployeeRole employeeRole;

    @Enumerated(EnumType.STRING)
    private EmployeeTier employeeTier;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;
}
