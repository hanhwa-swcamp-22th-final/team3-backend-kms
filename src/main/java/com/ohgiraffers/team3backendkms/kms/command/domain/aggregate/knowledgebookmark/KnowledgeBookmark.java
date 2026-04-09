package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgebookmark;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 지식 게시글 북마크 엔티티 — knowledge_bookmark 테이블 매핑
 * - 직원이 마음에 드는 게시글을 즐겨찾기하는 기능
 * - PK: (article_id + employee_id) 복합키 — KnowledgeBookmarkId 참조
 * - 비즈니스 메서드 없음 (추가/삭제만 존재)
 */
@Entity
@Table(name = "knowledge_bookmark")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class KnowledgeBookmark {

    // 복합 PK (article_id + employee_id)
    @EmbeddedId
    private KnowledgeBookmarkId id;

    // JPA Auditing — INSERT 시 자동 세팅, UPDATE 시 변경 안 됨
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
