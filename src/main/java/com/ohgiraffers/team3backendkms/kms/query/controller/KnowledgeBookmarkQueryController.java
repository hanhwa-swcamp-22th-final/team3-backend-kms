package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeBookmarkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 북마크 Query 컨트롤러
 * - GET /api/kms/my/bookmarks : 내 북마크 목록 조회
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/kms/my")
@PreAuthorize("hasAuthority('WORKER')")
public class KnowledgeBookmarkQueryController {

    private final KnowledgeBookmarkQueryService bookmarkQueryService;

    // 내 북마크 목록 조회
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getMyBookmarks(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        List<ArticleReadDto> bookmarks = bookmarkQueryService.getMyBookmarks(
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("내 북마크 목록을 조회했습니다.", bookmarks));
    }
}
