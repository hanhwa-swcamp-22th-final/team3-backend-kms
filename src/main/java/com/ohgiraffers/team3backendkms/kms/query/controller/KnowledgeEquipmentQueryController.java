package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.EquipmentReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeEquipmentQueryController {

    private final KnowledgeEquipmentQueryService knowledgeEquipmentQueryService;

    @GetMapping("/equipments")
    public ResponseEntity<ApiResponse<List<EquipmentReadDto>>> getAllEquipments() {
        List<EquipmentReadDto> equipments = knowledgeEquipmentQueryService.getAllEquipments();
        return ResponseEntity.ok(ApiResponse.success("설비 목록을 조회했습니다.", equipments));
    }
}
