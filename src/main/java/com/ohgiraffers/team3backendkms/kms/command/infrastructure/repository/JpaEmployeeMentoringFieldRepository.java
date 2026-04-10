package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.employeementoringfield.EmployeeMentoringField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmployeeMentoringFieldRepository extends JpaRepository<EmployeeMentoringField, Long> {

    boolean existsByEmployeeIdAndMentoringField(Long employeeId, String mentoringField);
}
