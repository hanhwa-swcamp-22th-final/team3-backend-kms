package com.ohgiraffers.team3backendkms.auth.command.application.controller;

import com.ohgiraffers.team3backendkms.auth.command.application.dto.request.EmployeeRegisterRequest;
import com.ohgiraffers.team3backendkms.auth.command.application.service.EmployeeService;
import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /* 직원 등록 — ADMIN, HRM 전용 */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HRM')")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(@RequestBody EmployeeRegisterRequest request) {
        Long employeeId = employeeService.register(request);
        return ResponseEntity.ok(ApiResponse.success(employeeId));
    }
}
