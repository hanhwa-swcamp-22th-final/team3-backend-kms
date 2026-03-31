package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeArticleService;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleQueryController {

    private final KnowledgeArticleQueryService knowledgeArticleQueryService;
    private final KnowledgeArticleService knowledgeArticleService;

    /* 지식 목록 조회 */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getArticles(
            @ModelAttribute ArticleQueryRequest request
    ) {
        List<ArticleReadDto> articles = knowledgeArticleQueryService.getArticles(request);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /* 지식 상세 조회 — 조회 후 조회수 증가 (Command는 Controller에서 조율) */
    @GetMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleDetail(
            @PathVariable Long articleId
    ) {
        ArticleDetailDto detail = knowledgeArticleQueryService.getArticleDetail(articleId);
        knowledgeArticleService.incrementViewCount(articleId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
