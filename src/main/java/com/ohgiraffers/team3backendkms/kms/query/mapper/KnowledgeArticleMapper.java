package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleDetailDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ContributorRankDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.EquipmentDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.TagDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ApprovalQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.ArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
 // MyBatis == 매퍼 인터페이스
// 여기 선언한 메서드 이름과 XML의 id가 연결.
@Mapper
public interface KnowledgeArticleMapper {
  // 지식 목록 조회용 메서드

    List<ArticleReadDto> findArticles(ArticleQueryRequest request);

  // 지식 문서 상세 1건 조회용 메서
    Optional<ArticleDetailDto> findArticleById(Long articleId);

  // 월간 기여자 랭킹 조회용 메서드
    List<ContributorRankDto> findTopContributors(Map<String, Object> params);

  // 추천 문서 목록 조회용 메서드
    List<ArticleReadDto> findRecommendations();

  // 승인 화면 통계 조회용 메서드
    ApprovalStatsDto findApprovalStats();

  // 승인 대기 목록 조회용 메서드
    List<ApprovalArticleDto> findApprovalArticles(ApprovalQueryRequest request);

  // 승인 상세 조회용 메서
    Optional<ApprovalArticleDetailDto> findApprovalArticleById(Long articleId);

  // 전체 태그 목록 조회용 메서
    List<TagDto> findAllTags();

//전체 설비 목록 조회용 메서드
    List<EquipmentDto> findAllEquipments();
}
