package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaMentoringRepository extends JpaRepository<Mentoring, Long> {

    boolean existsByRequestId(Long requestId);

    Optional<Mentoring> findByRequestId(Long requestId);
}
