package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleMyQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/articles/stats")
    public ResponseEntity<ApiResponse<MyArticleStatsDto>> getMyArticleStats(
            @RequestParam @Positive(message = "authorId는 양수여야 합니다.") Long authorId
    ) {
        MyArticleStatsDto stats = knowledgeArticleMyQueryService.getMyArticleStats(authorId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<MyArticleDto>>> getMyArticles(
            @Valid @ModelAttribute MyArticleQueryRequest request
    ) {
        List<MyArticleDto> articles = knowledgeArticleMyQueryService.getMyArticles(request);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
