package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface KnowledgeArticleMapper {

    List<ArticleReadDto> findArticles(ArticleQueryRequest request);

    Optional<ArticleDetailDto> findArticleById(Long articleId);

    List<ContributorRankDto> findTopContributors(Map<String, Object> params);

    List<ArticleReadDto> findRecommendations();

    ApprovalStatsDto findApprovalStats();

    List<ApprovalArticleDto> findApprovalArticles(ApprovalQueryRequest request);

    Optional<ApprovalArticleDetailDto> findApprovalArticleById(Long articleId);

    MyArticleStatsDto findMyArticleStats(Long authorId);

    List<MyArticleDto> findMyArticles(MyArticleQueryRequest request);
}
