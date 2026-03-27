package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleQueryRequest;
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

    /* 지식 목록 조회 */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleReadDto>>> getArticles(
            @ModelAttribute ArticleQueryRequest request
    ) {
        List<ArticleReadDto> articles = knowledgeArticleQueryService.getArticles(request);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /* 지식 상세 조회 */
    @GetMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleDetail(
            @PathVariable Long articleId
    ) {
        ArticleDetailDto detail = knowledgeArticleQueryService.getArticleDetail(articleId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
