package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMentoringRepository extends JpaRepository<Mentoring, Long> {

    boolean existsByRequestId(Long requestId);
}
