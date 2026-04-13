package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringRequestCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringRequestUpdateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.MentoringCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/mentoring/requests")
public class WorkerMentoringController {

    private final MentoringCommandService mentoringCommandService;

    /* 멘토링 신청 등록 (B/C 등급 Worker만) */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createRequest(
            @Valid @RequestBody MentoringRequestCreateRequest request
    ) {
        Long requestId = mentoringCommandService.createRequest(
                request.getMenteeId(),
                request.getArticleId(),
                request.getMentoringField(),
                request.getRequestTitle(),
                request.getRequestContent(),
                request.getMentoringDurationWeeks(),
                request.getMentoringFrequency(),
                request.getRequestPriority()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("멘토링 신청이 등록되었습니다.", requestId));
    }

    /* 멘토링 신청 수정 (PENDING 상태, 본인만) */
    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> updateRequest(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long requestId,
            @Valid @RequestBody MentoringRequestUpdateRequest request
    ) {
        mentoringCommandService.updateRequest(
                requestId,
                request.getMenteeId(),
                request.getRequestTitle(),
                request.getRequestContent(),
                request.getMentoringDurationWeeks(),
                request.getMentoringFrequency(),
                request.getRequestPriority()
        );
        return ResponseEntity.ok(ApiResponse.success("멘토링 신청이 수정되었습니다.", null));
    }
}
