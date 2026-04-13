package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringListDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.mentoring.MentoringRequestListDto;
import com.ohgiraffers.team3backendkms.kms.query.service.MentoringQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/mentoring")
public class MentoringQueryController {

    private final MentoringQueryService mentoringQueryService;

    /* 멘토가 볼 수 있는 PENDING 신청 목록 (분야 필터 선택적) */
    @GetMapping("/requests/pending")
    public ResponseEntity<ApiResponse<List<MentoringRequestListDto>>> getPendingRequests(
            @RequestParam Long mentorId,
            @RequestParam(required = false) String field
    ) {
        List<MentoringRequestListDto> result = mentoringQueryService.getPendingRequestsForMentor(mentorId, field);
        return ResponseEntity.ok(ApiResponse.success("멘토링 신청 목록을 조회했습니다.", result));
    }

    /* 멘티 본인 신청 목록 */
    @GetMapping("/requests/my")
    public ResponseEntity<ApiResponse<List<MentoringRequestListDto>>> getMyRequests(
            @RequestParam Long menteeId
    ) {
        List<MentoringRequestListDto> result = mentoringQueryService.getMyRequests(menteeId);
        return ResponseEntity.ok(ApiResponse.success("내 멘토링 신청 목록을 조회했습니다.", result));
    }

    /* 진행중/완료 멘토링 목록 — 멘토 기준 */
    @GetMapping("/mentor")
    public ResponseEntity<ApiResponse<List<MentoringListDto>>> getMentoringsByMentor(
            @RequestParam Long mentorId
    ) {
        List<MentoringListDto> result = mentoringQueryService.getMentoringsByMentor(mentorId);
        return ResponseEntity.ok(ApiResponse.success("멘토 기준 멘토링 목록을 조회했습니다.", result));
    }

    /* 진행중/완료 멘토링 목록 — 멘티 기준 */
    @GetMapping("/mentee")
    public ResponseEntity<ApiResponse<List<MentoringListDto>>> getMentoringsByMentee(
            @RequestParam Long menteeId
    ) {
        List<MentoringListDto> result = mentoringQueryService.getMentoringsByMentee(menteeId);
        return ResponseEntity.ok(ApiResponse.success("멘티 기준 멘토링 목록을 조회했습니다.", result));
    }

    /* 전체 진행중 멘토링 목록 (사이드바 위젯용) */
    @GetMapping("/in-progress")
    public ResponseEntity<ApiResponse<List<MentoringListDto>>> getInProgressMentorings() {
        List<MentoringListDto> result = mentoringQueryService.getInProgressMentorings();
        return ResponseEntity.ok(ApiResponse.success("진행중 멘토링 목록을 조회했습니다.", result));
    }

    /* 전체 PENDING 요청 목록 (사이드바 위젯용) */
    @GetMapping("/requests/all-pending")
    public ResponseEntity<ApiResponse<List<MentoringRequestListDto>>> getAllPendingRequests() {
        List<MentoringRequestListDto> result = mentoringQueryService.getAllPendingRequests();
        return ResponseEntity.ok(ApiResponse.success("전체 멘토링 신청 목록을 조회했습니다.", result));
    }
}
