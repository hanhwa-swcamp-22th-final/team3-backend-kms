package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleCommandService;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeHubStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleApprovalQueryService;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleQueryController {

    private final KnowledgeArticleQueryService knowledgeArticleQueryService;
    private final KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;
    private final KnowledgeArticleCommandService knowledgeArticleCommandService;

    @GetMapping(value = "/articles", params = "!stat")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getArticles(
            Authentication authentication,
            @ModelAttribute ArticleQueryRequest request
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        applyRequester(request, userDetails);
        List<ArticleReadDto> articles = knowledgeArticleQueryService.getArticles(request);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 목록을 조회했습니다.", articles));
    }

    @GetMapping(value = "/articles", params = "stat=approval")
    public ResponseEntity<ApiResponse<List<ApprovalArticleDto>>> getApprovalArticles(
            @ModelAttribute ApprovalQueryRequest request
    ) {
        List<ApprovalArticleDto> articles = knowledgeArticleApprovalQueryService.getApprovalArticles(request);
        return ResponseEntity.ok(ApiResponse.success("승인 대기 문서 목록을 조회했습니다.", articles));
    }

    @GetMapping(value = "/articles/{articleId}", params = "!stat")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleDetail(
            Authentication authentication,
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId
    ) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        Long requesterId = userDetails != null ? userDetails.getEmployeeId() : null;
        knowledgeArticleCommandService.incrementViewCount(articleId, requesterId);
        ArticleDetailDto detail = knowledgeArticleQueryService.getArticleDetail(articleId, requesterId);
        return ResponseEntity.ok(ApiResponse.success("지식 문서 상세를 조회했습니다.", detail));
    }

    @GetMapping(value = "/articles/{articleId}", params = "stat=approval")
    public ResponseEntity<ApiResponse<ApprovalArticleDetailDto>> getApprovalArticleDetail(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long articleId
    ) {
        ApprovalArticleDetailDto detail = knowledgeArticleApprovalQueryService.getApprovalArticleById(articleId);
        return ResponseEntity.ok(ApiResponse.success("승인 대상 문서 상세를 조회했습니다.", detail));
    }

    @GetMapping(value = "/stats", params = "stat=approval")
    public ResponseEntity<ApiResponse<ApprovalStatsDto>> getApprovalStats() {
        ApprovalStatsDto stats = knowledgeArticleApprovalQueryService.getApprovalStats();
        return ResponseEntity.ok(ApiResponse.success("승인 통계를 조회했습니다.", stats));
    }

    @GetMapping(value = "/stats", params = "stat=hub")
    public ResponseEntity<ApiResponse<KnowledgeHubStatsDto>> getKnowledgeHubStats() {
        KnowledgeHubStatsDto stats = knowledgeArticleQueryService.getKnowledgeHubStats();
        return ResponseEntity.ok(ApiResponse.success("KMS 허브 통계를 조회했습니다.", stats));
    }

    @GetMapping("/articles/contributors")
    public ResponseEntity<ApiResponse<List<ContributorRankDto>>> getTopContributors(
            @RequestParam(value = "limit", defaultValue = "3") Integer limit
    ) {
        List<ContributorRankDto> contributors = knowledgeArticleQueryService.getTopContributors(limit);
        return ResponseEntity.ok(ApiResponse.success("기여자 순위를 조회했습니다.", contributors));
    }

    @GetMapping("/articles/recommendations")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getRecommendations() {
        List<ArticleReadDto> recommendations = knowledgeArticleQueryService.getRecommendations();
        return ResponseEntity.ok(ApiResponse.success("추천 문서를 조회했습니다.", recommendations));
    }

    private void applyRequester(ArticleQueryRequest request, EmployeeUserDetails userDetails) {
        if (request == null || userDetails == null) {
            return;
        }

        request.setRequesterId(userDetails.getEmployeeId());
        request.setRequesterRole(
                userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority())
                        .orElse(null)
        );
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
