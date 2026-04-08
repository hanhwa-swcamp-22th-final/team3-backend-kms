package com.ohgiraffers.team3backendkms.kms.query.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 쿼리 파라미터 바인딩에 필요 (예외적 Setter 허용)
@NoArgsConstructor
public class ArticleQueryRequest {

    private ArticleCategory category;   // 카테고리 필터
    private ArticleStatus status;       // 상태 필터
    private String sort;                // 정렬 (latest / popular)
    private String searchType;          // 검색 조건 (articleId / authorName / articleTitle)
    private String keyword;             // 검색어
    private Long articleIdKeyword;      // articleId 검색용 숫자 변환 결과
    private Long requesterId;           // 임시: JWT 연결 전 조회 요청자 ID
    private String requesterRole;       // 임시: JWT 연결 전 조회 요청자 역할
    private Integer page;
    private Integer size;
}
