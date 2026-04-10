package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMentoringEmployeeRepository extends JpaRepository<MentoringEmployee, Long> {
}
