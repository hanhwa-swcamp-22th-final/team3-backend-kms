package com.ohgiraffers.team3backendkms.auth.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaEmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmployeeEmail(String employeeEmail);
}
