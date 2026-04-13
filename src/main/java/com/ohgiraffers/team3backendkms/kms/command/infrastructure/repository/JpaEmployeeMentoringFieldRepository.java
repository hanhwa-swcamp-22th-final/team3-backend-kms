package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.employeementoringfield.EmployeeMentoringField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmployeeMentoringFieldRepository extends JpaRepository<EmployeeMentoringField, Long> {

    // 특정 직원이 해당 분야 멘토 자격이 있는지
    boolean existsByEmployeeIdAndMentoringField(Long employeeId, String mentoringField);
}
