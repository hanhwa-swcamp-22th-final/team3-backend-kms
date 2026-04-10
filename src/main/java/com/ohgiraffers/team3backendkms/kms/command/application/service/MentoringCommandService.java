package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.MentoringErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.Mentoring;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.MentoringStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeTier;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployee;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployeeRole;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.EmployeeMentoringFieldRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringEmployeeRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringCommandService {

    private static final List<MentoringRequestStatus> ACTIVE_REQUEST_STATUSES =
            List.of(MentoringRequestStatus.PENDING, MentoringRequestStatus.ACCEPTED);

    private final MentoringRequestRepository mentoringRequestRepository;
    private final MentoringEmployeeRepository mentoringEmployeeRepository;
    private final EmployeeMentoringFieldRepository employeeMentoringFieldRepository;
    private final MentoringRepository mentoringRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final IdGenerator idGenerator;

    public Long createRequest(
            Long menteeId,
            Long articleId,
            String mentoringField,
            String requestTitle,
            String requestContent,
            Integer mentoringDurationWeeks,
            String mentoringFrequency,
            RequestPriority requestPriority
    ) {
        MentoringEmployee mentee = mentoringEmployeeRepository.findById(menteeId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_004));

        validateMenteeEligibility(mentee);
        validateArticle(articleId);
        validateDuplicateRequest(menteeId, mentoringField, articleId);

        MentoringRequest mentoringRequest = MentoringRequest.builder()
                .requestId(idGenerator.generate())
                .menteeId(menteeId)
                .mentorId(null)
                .articleId(articleId)
                .mentoringField(mentoringField)
                .requestTitle(requestTitle)
                .requestContent(requestContent)
                .mentoringDurationWeeks(mentoringDurationWeeks)
                .mentoringFrequency(mentoringFrequency)
                .requestPriority(requestPriority)
                .requestStatus(MentoringRequestStatus.PENDING)
                .rejectReason(null)
                .rejectedMentorIds(null)
                .build();

        return mentoringRequestRepository.save(mentoringRequest).getRequestId();
    }

    public Long acceptRequest(Long requestId, Long mentorId) {
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_006));

        MentoringEmployee mentor = mentoringEmployeeRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_004));

        validateMentorEligibility(mentoringRequest, mentor);

        if (mentoringRepository.existsByRequestId(requestId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_010);
        }

        mentoringRequest.accept(mentorId);

        Mentoring mentoring = Mentoring.builder()
                .mentoringId(idGenerator.generate())
                .requestId(mentoringRequest.getRequestId())
                .mentorId(mentorId)
                .menteeId(mentoringRequest.getMenteeId())
                .mentoringStatus(MentoringStatus.IN_PROGRESS)
                .build();

        return mentoringRepository.save(mentoring).getMentoringId();
    }

    private void validateMenteeEligibility(MentoringEmployee mentee) {
        if (mentee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_002);
        }

        if (mentee.getEmployeeRole() != MentoringEmployeeRole.WORKER
                || (mentee.getEmployeeTier() != EmployeeTier.B && mentee.getEmployeeTier() != EmployeeTier.C)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_001);
        }
    }

    private void validateArticle(Long articleId) {
        if (articleId == null) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_011);
        }

        KnowledgeArticle article = knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_005));

        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_005);
        }
    }

    private void validateMentorEligibility(MentoringRequest mentoringRequest, MentoringEmployee mentor) {
        if (mentoringRequest.getRequestStatus() != MentoringRequestStatus.PENDING) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_007);
        }

        if (mentoringRequest.getMenteeId().equals(mentor.getEmployeeId())) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_009);
        }

        if (mentor.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_008);
        }

        boolean mentorRoleAllowed = mentor.getEmployeeRole() == MentoringEmployeeRole.TL
                || mentor.getEmployeeRole() == MentoringEmployeeRole.DL
                || (mentor.getEmployeeRole() == MentoringEmployeeRole.WORKER
                && (mentor.getEmployeeTier() == EmployeeTier.S || mentor.getEmployeeTier() == EmployeeTier.A));

        if (!mentorRoleAllowed) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_008);
        }

        if (!employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(
                mentor.getEmployeeId(), mentoringRequest.getMentoringField())) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_008);
        }
    }

    private void validateDuplicateRequest(Long menteeId, String mentoringField, Long articleId) {
        boolean duplicated = articleId == null
                ? mentoringRequestRepository.existsByMenteeIdAndMentoringFieldAndRequestStatusIn(
                        menteeId, mentoringField, ACTIVE_REQUEST_STATUSES)
                : mentoringRequestRepository.existsByMenteeIdAndMentoringFieldAndArticleIdAndRequestStatusIn(
                        menteeId, mentoringField, articleId, ACTIVE_REQUEST_STATUSES);

        if (duplicated) {
            throw new BusinessException(MentoringErrorCode.MENTORING_REQUEST_003);
        }
    }
}
