package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleMyQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/kms/my")
public class KnowledgeArticleMyQueryController {

    private final KnowledgeArticleMyQueryService knowledgeArticleMyQueryService;

    @GetMapping("/articles/stats")
    public ResponseEntity<ApiResponse<MyArticleStatsDto>> getMyArticleStats(
            Authentication authentication
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        MyArticleStatsDto stats = knowledgeArticleMyQueryService.getMyArticleStats(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("내 문서 통계를 조회했습니다.", stats));
    }

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<MyArticleDto>>> getMyArticles(
            Authentication authentication,
            @Valid @ModelAttribute MyArticleQueryRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        request.setAuthorId(userDetails.getEmployeeId());
        List<MyArticleDto> articles = knowledgeArticleMyQueryService.getMyArticles(request);
        return ResponseEntity.ok(ApiResponse.success("내 문서 목록을 조회했습니다.", articles));
    }

    @GetMapping("/articles/history")
    public ResponseEntity<ApiResponse<List<MyArticleHistoryDto>>> getMyRecentArticleHistory(
            Authentication authentication
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        List<MyArticleHistoryDto> history = knowledgeArticleMyQueryService.getMyRecentArticleHistory(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("내 최근 문서 이력을 조회했습니다.", history));
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
