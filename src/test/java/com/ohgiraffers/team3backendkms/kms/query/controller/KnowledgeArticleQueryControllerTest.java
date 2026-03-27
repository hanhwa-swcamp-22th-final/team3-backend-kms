package com.ohgiraffers.team3backendkms.kms.query.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest
class KnowledgeArticleQueryControllerTest {

    @Test
    @DisplayName("지식 목록 API 성공: 목록 JSON을 반환한다")
    void getArticles_success() {
    }

    @Test
    @DisplayName("지식 목록 API 성공: 쿼리 파라미터가 정상 바인딩된다")
    void getArticles_withQueryParams_success() {
    }

    @Test
    @DisplayName("지식 상세 API 성공: 상세 JSON을 반환한다")
    void getArticleDetail_success() {
    }

    @Test
    @DisplayName("지식 상세 API 실패: 문서가 없으면 404를 반환한다")
    void getArticleDetail_whenNotFound_thenNotFound() {
    }
}
