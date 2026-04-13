package com.ohgiraffers.team3backendkms.kms.command.application.controller.mentor;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringAcceptRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringCompleteRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringRejectRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.MentoringCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/mentoring")
public class MentorMentoringController {

    private final MentoringCommandService mentoringCommandService;

    /* 멘토링 신청 수락 (해당 분야 멘토: S/A Worker, TL, DL) */
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Long>> acceptRequest(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long requestId,
            @Valid @RequestBody MentoringAcceptRequest request
    ) {
        Long mentoringId = mentoringCommandService.acceptRequest(
                requestId,
                request.getMentorId(),
                request.getMentorRole(),
                request.getMentorTier()
        );
        return ResponseEntity.ok(ApiResponse.success("멘토링 신청을 수락했습니다.", mentoringId));
    }

    /* 멘토 개인 거절 (해당 요청에서 본인만 제외, request 전체 상태 유지) */
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long requestId,
            @Valid @RequestBody MentoringRejectRequest request
    ) {
        mentoringCommandService.rejectRequest(requestId, request.getMentorId());
        return ResponseEntity.ok(ApiResponse.success("멘토링 신청을 거절했습니다.", null));
    }

    /* 멘토링 완료 처리 (담당 멘토만) */
    @PutMapping("/{mentoringId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeMentoring(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long mentoringId,
            @Valid @RequestBody MentoringCompleteRequest request
    ) {
        mentoringCommandService.completeMentoring(mentoringId, request.getMentorId());
        return ResponseEntity.ok(ApiResponse.success("멘토링이 완료 처리되었습니다.", null));
    }
}
