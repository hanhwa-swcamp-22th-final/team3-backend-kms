package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringListDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringRequestListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MentoringMapper {

    // 멘토가 볼 수 있는 PENDING 요청 목록
    // (해당 분야 자격 있음 + 본인이 거절하지 않은 것)
    List<MentoringRequestListDto> findPendingRequestsForMentor(
            @Param("mentorId") Long mentorId,
            @Param("mentoringField") String mentoringField
    );

    // 멘티 본인 신청 목록
    List<MentoringRequestListDto> findRequestsByMentee(@Param("menteeId") Long menteeId);

    // 진행중/완료 멘토링 목록 (mentor 또는 mentee 기준)
    List<MentoringListDto> findMentoringsByMentor(@Param("mentorId") Long mentorId);

    List<MentoringListDto> findMentoringsByMentee(@Param("menteeId") Long menteeId);

    // 전체 진행중 멘토링 목록 (사이드바 위젯용)
    List<MentoringListDto> findInProgressMentorings();

    // 전체 PENDING 요청 목록 (사이드바 위젯용 — 분야 필터 없이)
    List<MentoringRequestListDto> findAllPendingRequests();
}
