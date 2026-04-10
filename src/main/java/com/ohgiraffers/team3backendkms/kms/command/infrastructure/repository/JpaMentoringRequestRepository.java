package com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface JpaMentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {

    boolean existsByMenteeIdAndMentoringFieldAndRequestStatusIn(
            Long menteeId,
            String mentoringField,
            Collection<MentoringRequestStatus> requestStatuses
    );

    boolean existsByMenteeIdAndMentoringFieldAndArticleIdAndRequestStatusIn(
            Long menteeId,
            String mentoringField,
            Long articleId,
            Collection<MentoringRequestStatus> requestStatuses
    );
}
