package com.ohgiraffers.team3backendkms.kms.query.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KnowledgeArticleQueryMapperTest {

    @Test
    @DisplayName("지식 목록 조회 성공: 전체 목록을 조회한다")
    void findArticles_success() {
    }

    @Test
    @DisplayName("지식 목록 조회 성공: 카테고리 필터가 반영된다")
    void findArticles_withCategoryFilter_success() {
    }

    @Test
    @DisplayName("지식 목록 조회 성공: 정렬 조건이 반영된다")
    void findArticles_withSort_success() {
    }

    @Test
    @DisplayName("지식 상세 조회 성공: 문서 상세를 조회한다")
    void findArticleById_success() {
    }

    @Test
    @DisplayName("지식 상세 조회 실패: 존재하지 않는 ID면 empty를 반환한다")
    void findArticleById_whenUnknownId_thenEmpty() {
    }
}
