package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeBookmarkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
            Authentication authentication) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        List<ArticleReadDto> bookmarks = bookmarkQueryService.getMyBookmarks(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("내 북마크 목록을 조회했습니다.", bookmarks));
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
