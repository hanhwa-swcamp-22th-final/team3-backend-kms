package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeBookmarkQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/kms/my")
public class KnowledgeBookmarkQueryController {

    private final KnowledgeBookmarkQueryService bookmarkQueryService;

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getMyBookmarks(
            @RequestParam @Positive(message = "employeeId는 양수여야 합니다.") Long employeeId) {
        List<ArticleReadDto> bookmarks = bookmarkQueryService.getMyBookmarks(employeeId);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }
}
