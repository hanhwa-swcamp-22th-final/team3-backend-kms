package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringListDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringRequestListDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.MentoringMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringQueryService {

    private final MentoringMapper mentoringMapper;

    public List<MentoringRequestListDto> getPendingRequestsForMentor(Long mentorId, String field) {
        return mentoringMapper.findPendingRequestsForMentor(mentorId, field);
    }

    public List<MentoringRequestListDto> getMyRequests(Long menteeId) {
        return mentoringMapper.findRequestsByMentee(menteeId);
    }

    public List<MentoringListDto> getMentoringsByMentor(Long mentorId) {
        return mentoringMapper.findMentoringsByMentor(mentorId);
    }

    public List<MentoringListDto> getMentoringsByMentee(Long menteeId) {
        return mentoringMapper.findMentoringsByMentee(menteeId);
    }

    public List<MentoringListDto> getInProgressMentorings() {
        return mentoringMapper.findInProgressMentorings();
    }

    public List<MentoringRequestListDto> getAllPendingRequests() {
        return mentoringMapper.findAllPendingRequests();
    }
}
