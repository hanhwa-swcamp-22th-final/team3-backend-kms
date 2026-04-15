package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.PendingArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeHubStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.PendingArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface KnowledgeArticleMapper {

    List<ArticleReadDto> findArticles(ArticleQueryRequest request);

    Optional<ArticleDetailDto> findArticleById(@Param("articleId") Long articleId, @Param("requesterId") Long requesterId);

    KnowledgeHubStatsDto findKnowledgeHubStats();

    List<ContributorRankDto> findTopContributors(Map<String, Object> params);

    List<ArticleReadDto> findRecommendations();

    List<ArticleReadDto> findSkillGapRecommendations(Map<String, Object> params);

    PendingArticleStatsDto findPendingStats(Long requesterId);

    List<PendingArticleDto> findPendingArticles(PendingArticleQueryRequest request);

    Optional<PendingArticleDetailDto> findPendingArticleById(@Param("articleId") Long articleId, @Param("requesterId") Long requesterId);

    MyArticleStatsDto findMyArticleStats(Long authorId);

    List<MyArticleDto> findMyArticles(@Param("authorId") Long authorId, @Param("request") MyArticleQueryRequest request);

    List<MyArticleHistoryDto> findMyRecentArticleHistory(Long authorId);
}
