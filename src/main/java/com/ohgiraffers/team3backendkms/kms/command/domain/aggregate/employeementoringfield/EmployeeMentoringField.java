package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.employeementoringfield;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_mentoring_field")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeMentoringField {

    @Id
    private Long employeeMentoringFieldId;

    private Long employeeId;
    private String mentoringField;
}
