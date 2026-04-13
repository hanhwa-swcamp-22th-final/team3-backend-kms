package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaMentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {

    // 중복 신청 확인: 같은 멘티 + 같은 분야 + PENDING/ACCEPTED
    boolean existsByMenteeIdAndMentoringFieldAndRequestStatusIn(
            Long menteeId,
            String mentoringField,
            List<MentoringRequestStatus> statuses
    );

    // 비관적 락으로 조회 (동시 수락 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM MentoringRequest r WHERE r.requestId = :requestId")
    Optional<MentoringRequest> findByIdWithLock(@Param("requestId") Long requestId);
}
