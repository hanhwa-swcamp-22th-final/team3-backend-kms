package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.MentoringErrorCode;
import com.ohgiraffers.team3backendkms.common.exception.ResourceNotFoundException;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.Mentoring;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.MentoringStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployee;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository.JpaEmployeeMentoringFieldRepository;
import com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository.JpaMentoringEmployeeRepository;
import com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository.JpaMentoringRepository;
import com.ohgiraffers.team3backendkms.kms.command.infrastructure.repository.JpaMentoringRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringCommandService {

    private final JpaMentoringRequestRepository mentoringRequestRepository;
    private final JpaMentoringRepository mentoringRepository;
    private final JpaEmployeeMentoringFieldRepository employeeMentoringFieldRepository;
    private final JpaMentoringEmployeeRepository mentoringEmployeeRepository;

    // ── 멘토링 신청 등록 (mentee: B/C 등급 worker) ─────────────────
    public Long createRequest(Long menteeId, Long articleId, String field,
                              String title, String content, Integer durationWeeks,
                              String frequency, String requestPriority) {
        MentoringEmployee mentee = getEmployee(menteeId);
        validateMenteeEligibility(mentee);

        boolean duplicate = mentoringRequestRepository
                .existsByMenteeIdAndMentoringFieldAndRequestStatusIn(
                        menteeId, field, List.of(MentoringRequestStatus.PENDING, MentoringRequestStatus.ACCEPTED));
        if (duplicate) {
            throw new BusinessException(MentoringErrorCode.MENTORING_011);
        }

        MentoringRequest request = MentoringRequest.builder()
                .menteeId(menteeId)
                .articleId(articleId)
                .mentoringField(field)
                .requestTitle(title)
                .requestContent(content)
                .mentoringDurationWeeks(durationWeeks)
                .mentoringFrequency(frequency)
                .requestPriority(parseRequestPriority(requestPriority))
                .requestStatus(MentoringRequestStatus.PENDING)
                .build();

        return mentoringRequestRepository.save(request).getRequestId();
    }

    // ── 멘토링 신청 수정 (PENDING 상태, 본인만) ────────────────────
    public void updateRequest(Long requestId, Long menteeId, String title, String content,
                              Integer durationWeeks, String frequency, String requestPriority) {
        MentoringRequest request = findRequestById(requestId);

        if (!request.getMenteeId().equals(menteeId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_012);
        }

        request.update(title, content, durationWeeks, frequency, parseRequestPriority(requestPriority));
    }

    // ── 멘토링 수락 (해당 분야 멘토, Pessimistic Lock) ─────────────
    public Long acceptRequest(Long requestId, Long mentorId) {
        MentoringEmployee mentor = getEmployee(mentorId);
        validateMentorEligibility(mentor);

        MentoringRequest request = mentoringRequestRepository.findByIdWithLock(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_NOT_FOUND));

        if (request.getMenteeId().equals(mentorId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_015);
        }

        if (!employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(mentorId, request.getMentoringField())) {
            throw new BusinessException(MentoringErrorCode.MENTORING_020);
        }

        // 락 획득 후 재검증
        if (request.getRequestStatus() != MentoringRequestStatus.PENDING) {
            throw new BusinessException(MentoringErrorCode.MENTORING_014);
        }

        if (mentoringRepository.existsByRequestId(requestId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_014);
        }

        request.accept(mentorId);

        Mentoring mentoring = Mentoring.builder()
                .requestId(requestId)
                .mentorId(mentorId)
                .menteeId(request.getMenteeId())
                .mentoringStatus(MentoringStatus.IN_PROGRESS)
                .build();

        return mentoringRepository.save(mentoring).getMentoringId();
    }

    // ── 멘토 개인 거절 (해당 요청 내 목록에서만 제외) ──────────────
    public void rejectRequest(Long requestId, Long mentorId) {
        MentoringRequest request = findRequestById(requestId);

        if (request.getRequestStatus() != MentoringRequestStatus.PENDING) {
            throw new BusinessException(MentoringErrorCode.MENTORING_014);
        }

        if (request.isRejectedBy(mentorId)) {
            throw new BusinessException(MentoringErrorCode.MENTORING_022);
        }

        if (!employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(mentorId, request.getMentoringField())) {
            throw new BusinessException(MentoringErrorCode.MENTORING_020);
        }

        request.addRejectedMentor(mentorId);
    }

    // ── 멘토링 완료 처리 (담당 멘토만) ────────────────────────────
    public void completeMentoring(Long mentoringId, Long mentorId) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_NOT_FOUND));

        mentoring.complete(mentorId);
    }

    // ── Admin: 요청 강제 종료 ──────────────────────────────────────
    public void expireRequest(Long requestId) {
        MentoringRequest request = findRequestById(requestId);
        request.expireReject();
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────
    private MentoringRequest findRequestById(Long requestId) {
        return mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_REQUEST_NOT_FOUND));
    }

    private MentoringEmployee getEmployee(Long employeeId) {
        return mentoringEmployeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(MentoringErrorCode.MENTORING_EMPLOYEE_NOT_FOUND));
    }

    private void validateMenteeEligibility(MentoringEmployee employee) {
        boolean isWorker = employee.getEmployeeRole() != null && employee.getEmployeeRole().name().equalsIgnoreCase("WORKER");
        boolean isLowTier = employee.getEmployeeTier() != null
                && ("B".equalsIgnoreCase(employee.getEmployeeTier().name()) || "C".equalsIgnoreCase(employee.getEmployeeTier().name()));
        if (!isWorker || !isLowTier) {
            throw new BusinessException(MentoringErrorCode.MENTORING_010);
        }
    }

    private void validateMentorEligibility(MentoringEmployee employee) {
        if (employee.getEmployeeRole() == null) {
            throw new BusinessException(MentoringErrorCode.MENTORING_021);
        }

        if ("TL".equalsIgnoreCase(employee.getEmployeeRole().name()) || "DL".equalsIgnoreCase(employee.getEmployeeRole().name())) return;

        boolean isWorker = "WORKER".equalsIgnoreCase(employee.getEmployeeRole().name());
        boolean isHighTier = employee.getEmployeeTier() != null
                && ("S".equalsIgnoreCase(employee.getEmployeeTier().name()) || "A".equalsIgnoreCase(employee.getEmployeeTier().name()));
        if (!isWorker || !isHighTier) {
            throw new BusinessException(MentoringErrorCode.MENTORING_021);
        }
    }

    private RequestPriority parseRequestPriority(String requestPriority) {
        if (requestPriority == null || requestPriority.isBlank()) {
            return null;
        }
        return RequestPriority.valueOf(requestPriority.trim().toUpperCase());
    }
}
