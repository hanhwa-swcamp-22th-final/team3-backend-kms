package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeHubStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.PendingArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.PendingArticleQueryService;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleQueryController {

    private final KnowledgeArticleQueryService knowledgeArticleQueryService;
    private final PendingArticleQueryService pendingArticleQueryService;
    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    /* 지식 목록 조회 */
    @GetMapping(value = "/articles", params = "!status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DL', 'TL', 'WORKER')")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getArticles(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @ModelAttribute ArticleQueryRequest request
    ) {
        // 목록 권한 분기는 mapper 에서 requesterId/requesterRole 조합으로 처리한다.
        request.setRequesterId(userDetails.getEmployeeId());
        request.setRequesterRole(userDetails.getAuthorities().iterator().next().getAuthority());
        List<ArticleReadDto> articles = knowledgeArticleQueryService.getArticles(request);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 목록을 조회했습니다.", articles));
    }

    /* 승인 대기 목록 조회 - 공통 articles 경로에서 status=pending 으로 분기 */
    @GetMapping(value = "/articles", params = "status=pending")
    @PreAuthorize("hasAnyAuthority('DL', 'TL')")
    public ResponseEntity<ApiResponse<List<PendingArticleDto>>> getPendingArticles(
            @ModelAttribute PendingArticleQueryRequest request
    ) {
        // 승인 대기 조회는 기존 /approval 경로를 없애고 공통 articles 경로에서 status 로 분기한다.
        List<PendingArticleDto> articles = pendingArticleQueryService.getPendingArticles(request);
        return ResponseEntity.ok(ApiResponse.success("승인 대기 문서 목록을 조회했습니다.", articles));
    }

    /* 지식 상세 조회 — 조회 후 조회수 증가 (Command는 Controller에서 조율) */
    @GetMapping(value = "/articles/{articleId}", params = "!status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DL', 'TL', 'WORKER')")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleDetail(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        Long currentRequesterId = userDetails.getEmployeeId();
        // 조회수는 상세 응답 전에 증가시켜야 첫 조회 결과에도 최신 viewCount 가 반영된다.
        knowledgeArticleCommandService.incrementViewCount(articleId, currentRequesterId);
        ArticleDetailDto detail = knowledgeArticleQueryService.getArticleDetail(articleId, currentRequesterId);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 상세를 조회했습니다.", detail));
    }

    /* 승인 상세 조회 - 공통 articles 경로에서 status=pending 으로 분기 */
    @GetMapping(value = "/articles/{articleId}", params = "status=pending")
    @PreAuthorize("hasAnyAuthority('DL ', ' TL ')")
    public ResponseEntity<ApiResponse<PendingArticleDetailDto>> getPendingArticleDetail(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId
    ) {
        PendingArticleDetailDto detail = pendingArticleQueryService.getPendingArticleById(articleId);
        return ResponseEntity.ok(ApiResponse.success("승인 대상 문서 상세를 조회했습니다.", detail));
    }

    /* 승인 통계 조회 - 공통 stats 경로에서 status=pending 으로 분기 */
    @GetMapping(value = "/stats", params = "status=pending")
    @PreAuthorize("hasAnyAuthority('DL', 'TL')")
    public ResponseEntity<ApiResponse<PendingArticleStatsDto>> getPendingStats() {
        PendingArticleStatsDto stats = pendingArticleQueryService.getPendingStats();
        return ResponseEntity.ok(ApiResponse.success("승인 통계를 조회했습니다.", stats));
    }

    /* KMS 허브 통계 조회 */
    @GetMapping(value = "/stats", params = "status=hub")
    @PreAuthorize("hasAnyAuthority(  'ADMIN', 'DL', 'TL', 'WORKER')")
    public ResponseEntity<ApiResponse<KnowledgeHubStatsDto>> getKnowledgeHubStats() {
        KnowledgeHubStatsDto stats = knowledgeArticleQueryService.getKnowledgeHubStats();
        return ResponseEntity.ok(ApiResponse.success("KMS 허브 통계를 조회했습니다.", stats));
    }

    /* KMS 허브 기여자 랭킹 조회 */
    @GetMapping("/articles/contributors")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DL', 'TL', 'WORKER')")
    public ResponseEntity<ApiResponse<List<ContributorRankDto>>> getTopContributors(
            @RequestParam(value = "limit", defaultValue = "3") Integer limit
    ) {
        List<ContributorRankDto> contributors = knowledgeArticleQueryService.getTopContributors(limit);
        return ResponseEntity.ok(ApiResponse.success("기여자 랭킹을 조회했습니다.", contributors));
    }

    /* AI 지식 추천 조회 (APPROVED 문서 중 조회수 높은 순 TOP 3) */
    @GetMapping("/articles/recommendations")
    @PreAuthorize("hasAnyAuthority('ADMIN ', 'DL', 'TL', 'WORKER')")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getRecommendations() {
        List<ArticleReadDto> recommendations = knowledgeArticleQueryService.getRecommendations();
        return ResponseEntity.ok(ApiResponse.success("추천 문서를 조회했습니다.", recommendations));
    }
}
