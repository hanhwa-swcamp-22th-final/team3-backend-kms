package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * knowledge_bookmark 테이블의 복합 PK 클래스
 * - article_id + employee_id 조합이 PK (식별관계)
 * - 같은 직원이 같은 게시글을 중복 북마크하는 것을 DB 레벨에서 방지
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KnowledgeBookmarkId implements Serializable {

    // 북마크 대상 게시글 ID
    @Column(name = "article_id")
    private Long articleId;

    // 북마크를 한 직원 ID (게시글 작성자가 아님)
    @Column(name = "employee_id")
    private Long employeeId;

    // 복합키 동등성 비교 — JPA가 엔티티 식별에 사용
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeBookmarkId that = (KnowledgeBookmarkId) o;
        return Objects.equals(articleId, that.articleId) &&
               Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, employeeId);
    }
}
