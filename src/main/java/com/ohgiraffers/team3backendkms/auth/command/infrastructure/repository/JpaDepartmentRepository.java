package com.ohgiraffers.team3backendkms.auth.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDepartmentRepository extends JpaRepository<Department, Long> {
}
