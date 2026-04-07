package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeTagQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeTagQueryController {

    private final KnowledgeTagQueryService knowledgeTagQueryService;

    /* 전체 태그 목록 조회 */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<KnowledgeTagReadDto>>> getAllTags() {
        List<KnowledgeTagReadDto> tags = knowledgeTagQueryService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
