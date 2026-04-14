package com.ohgiraffers.team3backendkms.kms.query.dto.request;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 쿼리 파라미터 바인딩에 필요 (예외적 Setter 허용)
@NoArgsConstructor
public class MyArticleQueryRequest {

    private ArticleStatus status;   // 상태 필터 (없으면 전체)
    private Integer page;
    private Integer size;

    // MariaDB LIMIT/OFFSET은 산술식 불가 → offset을 미리 계산해서 전달
    public int getOffset() {
        return (page != null && size != null) ? page * size : 0;
    }
}
