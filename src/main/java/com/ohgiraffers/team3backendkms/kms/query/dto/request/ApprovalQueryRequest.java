package com.ohgiraffers.team3backendkms.kms.query.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 쿼리 파라미터 바인딩에 필요 (예외적 Setter 허용)
@NoArgsConstructor
public class ApprovalQueryRequest {

    private ArticleCategory category;   // 카테고리 필터
    private String sort;                // 정렬 (latest / popular)
    private String searchType;          // 검색 조건 (articleId / authorName / articleTitle)
    private String keyword;             // 검색어
    private Long articleIdKeyword;      // articleId 검색용 숫자 변환 결과
    private Integer page;
    private Integer size;

    // MariaDB LIMIT/OFFSET은 바인딩 파라미터 간 산술식을 허용하지 않아 미리 계산해서 전달한다.
    public int getOffset() {
        return (page != null && size != null) ? page * size : 0;
    }
}
