package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.query.dto.WorkerSkillGapResponse;
import com.ohgiraffers.team3backendkms.kms.query.service.WorkerSkillGapQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/workers/me")
public class WorkerSkillGapQueryController {

    private final WorkerSkillGapQueryService workerSkillGapQueryService;

    @GetMapping("/skill-gap")
    @PreAuthorize("hasAnyAuthority('WORKER')")
    public ResponseEntity<ApiResponse<WorkerSkillGapResponse>> getSkillGap(
            @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        WorkerSkillGapResponse response = workerSkillGapQueryService.getSkillGap(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("스킬갭 정보를 조회했습니다.", response));
    }
}
