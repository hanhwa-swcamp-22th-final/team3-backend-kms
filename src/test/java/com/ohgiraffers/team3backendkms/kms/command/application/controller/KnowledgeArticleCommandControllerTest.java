package com.ohgiraffers.team3backendkms.kms.command.application.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest
class KnowledgeArticleCommandControllerTest {

    @Test
    @DisplayName("지식 문서 등록 API 성공: 정상 응답을 반환한다")
    void register_success() {
    }

    @Test
    @DisplayName("지식 문서 등록 API 실패: 제목이 짧으면 400을 반환한다")
    void register_whenTitleTooShort_thenBadRequest() {
    }

    @Test
    @DisplayName("지식 문서 등록 API 실패: 본문이 짧으면 400을 반환한다")
    void register_whenContentTooShort_thenBadRequest() {
    }

    @Test
    @DisplayName("지식 문서 임시저장 API 성공: 정상 응답을 반환한다")
    void draft_success() {
    }

    @Test
    @DisplayName("지식 문서 승인 API 성공: 정상 응답을 반환한다")
    void approve_success() {
    }

    @Test
    @DisplayName("지식 문서 승인 API 실패: PENDING이 아니면 400을 반환한다")
    void approve_whenNotPending_thenBadRequest() {
    }

    @Test
    @DisplayName("지식 문서 반려 API 성공: 정상 응답을 반환한다")
    void reject_success() {
    }

    @Test
    @DisplayName("지식 문서 반려 API 실패: 반려 사유가 없으면 400을 반환한다")
    void reject_whenNoReason_thenBadRequest() {
    }

    @Test
    @DisplayName("지식 문서 삭제 API 성공: 정상 응답을 반환한다")
    void delete_success() {
    }

    @Test
    @DisplayName("지식 문서 삭제 API 실패: 본인 문서가 아니면 400을 반환한다")
    void delete_whenNotAuthor_thenBadRequest() {
    }
}
