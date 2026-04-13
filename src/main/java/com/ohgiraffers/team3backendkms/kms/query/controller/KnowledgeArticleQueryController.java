package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeHubStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleApprovalQueryService;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleQueryController {

    private final KnowledgeArticleQueryService knowledgeArticleQueryService;
    private final KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;
    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    /* 지식 목록 조회 */
    @GetMapping(value = "/articles", params = "!stat")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getArticles(
            @ModelAttribute ArticleQueryRequest request
    ) {
        List<ArticleReadDto> articles = knowledgeArticleQueryService.getArticles(request);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 목록을 조회했습니다.", articles));
    }

    /* 승인 대기 목록 조회 - 공통 articles 경로에서 stat=approval 로 분기 */
    @GetMapping(value = "/articles", params = "stat=approval")
    public ResponseEntity<ApiResponse<List<ApprovalArticleDto>>> getApprovalArticles(
            @ModelAttribute ApprovalQueryRequest request
    ) {
        List<ApprovalArticleDto> articles = knowledgeArticleApprovalQueryService.getApprovalArticles(request);
        return ResponseEntity.ok(ApiResponse.success("승인 대기 문서 목록을 조회했습니다.", articles));
    }

    /* 지식 상세 조회 — 조회 후 조회수 증가 (Command는 Controller에서 조율) */
    @GetMapping(value = "/articles/{articleId}", params = "!stat")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleDetail(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId,
            @RequestParam(required = false) Long requesterId
    ) {
        ArticleDetailDto detail = knowledgeArticleQueryService.getArticleDetail(articleId, requesterId);
        knowledgeArticleCommandService.incrementViewCount(articleId);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 상세를 조회했습니다.", detail));
    }

    /* 승인 상세 조회 - 공통 articles 경로에서 stat=approval 로 분기 */
    @GetMapping(value = "/articles/{articleId}", params = "stat=approval")
    public ResponseEntity<ApiResponse<ApprovalArticleDetailDto>> getApprovalArticleDetail(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId
    ) {
        ApprovalArticleDetailDto detail = knowledgeArticleApprovalQueryService.getApprovalArticleById(articleId);
        return ResponseEntity.ok(ApiResponse.success("승인 대상 문서 상세를 조회했습니다.", detail));
    }

    /* 승인 통계 조회 - 공통 stats 경로에서 stat=approval 로 분기 */
    @GetMapping(value = "/stats", params = "stat=approval")
    public ResponseEntity<ApiResponse<ApprovalStatsDto>> getApprovalStats() {
        ApprovalStatsDto stats = knowledgeArticleApprovalQueryService.getApprovalStats();
        return ResponseEntity.ok(ApiResponse.success("승인 통계를 조회했습니다.", stats));
    }

    /* KMS 허브 통계 조회 */
    @GetMapping(value = "/stats", params = "stat=hub")
    public ResponseEntity<ApiResponse<KnowledgeHubStatsDto>> getKnowledgeHubStats() {
        KnowledgeHubStatsDto stats = knowledgeArticleQueryService.getKnowledgeHubStats();
        return ResponseEntity.ok(ApiResponse.success("KMS 허브 통계를 조회했습니다.", stats));
    }

    /* KMS 허브 기여자 랭킹 조회 */
    @GetMapping("/articles/contributors")
    public ResponseEntity<ApiResponse<List<ContributorRankDto>>> getTopContributors(
            @RequestParam(value = "limit", defaultValue = "3") Integer limit
    ) {
        List<ContributorRankDto> contributors = knowledgeArticleQueryService.getTopContributors(limit);
        return ResponseEntity.ok(ApiResponse.success("기여자 랭킹을 조회했습니다.", contributors));
    }

    /* AI 지식 추천 조회 (APPROVED 문서 중 조회수 높은 순 TOP 3) */
    @GetMapping("/articles/recommendations")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getRecommendations() {
        List<ArticleReadDto> recommendations = knowledgeArticleQueryService.getRecommendations();
        return ResponseEntity.ok(ApiResponse.success("추천 문서를 조회했습니다.", recommendations));
    }
}
