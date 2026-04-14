package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleMyQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/kms/my")
public class KnowledgeArticleMyQueryController {

    private final KnowledgeArticleMyQueryService knowledgeArticleMyQueryService;

    // 내 게시글 상태
    @GetMapping("/articles/stats")
    public ResponseEntity<ApiResponse<MyArticleStatsDto>> getMyArticleStats(
            @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        MyArticleStatsDto stats = knowledgeArticleMyQueryService.getMyArticleStats(
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("내 문서 통계를 조회했습니다.", stats));
    }

    /* 내 문서 목록 조회 */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<MyArticleDto>>> getMyArticles(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @ModelAttribute MyArticleQueryRequest request
    ) {
        request.setAuthorId(userDetails.getEmployeeId());
        List<MyArticleDto> articles = knowledgeArticleMyQueryService.getMyArticles(request);
        return ResponseEntity.ok(ApiResponse.success("내 문서 목록을 조회했습니다.", articles));
    }

    /* 내 최근 문서 이력 조회 */
    @GetMapping("/articles/history")
    public ResponseEntity<ApiResponse<List<MyArticleHistoryDto>>> getMyRecentArticleHistory(
            @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        List<MyArticleHistoryDto> history = knowledgeArticleMyQueryService.getMyRecentArticleHistory(
                userDetails.getEmployeeId()
        );
        return ResponseEntity.ok(ApiResponse.success("내 최근 문서 이력을 조회했습니다.", history));
    }
}
