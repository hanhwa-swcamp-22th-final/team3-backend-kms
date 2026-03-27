package com.ohgiraffers.team3backendkms.kms.query.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleQueryServiceTest {

    @Test
    @DisplayName("지식 목록 조회 성공: 목록 응답 DTO를 반환한다")
    void getArticles_success() {
    }

    @Test
    @DisplayName("지식 목록 조회 성공: 데이터가 없으면 빈 목록을 반환한다")
    void getArticles_whenNoData_thenReturnEmptyList() {
    }

    @Test
    @DisplayName("지식 상세 조회 성공: 상세 응답 DTO를 반환한다")
    void getArticleDetail_success() {
    }

    @Test
    @DisplayName("지식 상세 조회 실패: 문서가 없으면 예외가 발생한다")
    void getArticleDetail_whenNotFound_thenThrow() {
    }
}
