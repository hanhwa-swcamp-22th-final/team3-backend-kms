package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringRequestAcceptRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.MentoringRequestCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.MentoringCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/mentoring/requests")
public class WorkerMentoringController {

    private final MentoringCommandService mentoringCommandService;

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
                .body(ApiResponse.success("멘토링 요청이 등록되었고 멘토 수락 대기 상태가 되었습니다.", requestId));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<Long>> acceptRequest(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long requestId,
            @Valid @RequestBody MentoringRequestAcceptRequest request
    ) {
        Long mentoringId = mentoringCommandService.acceptRequest(requestId, request.getMentorId());
        return ResponseEntity.ok(ApiResponse.success("멘토링 요청이 수락되었고 진행 중 상태가 시작되었습니다.", mentoringId));
    }
}
