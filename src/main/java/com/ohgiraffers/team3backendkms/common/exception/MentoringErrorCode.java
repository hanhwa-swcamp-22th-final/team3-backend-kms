package com.ohgiraffers.team3backendkms.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MentoringErrorCode implements KmsErrorCode {

    // ── 멘토링 신청 입력값 검증 ────────────────────────────────────
    MENTORING_001(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_001] 신청 제목은 1자 이상 50자 이하여야 합니다."),
    MENTORING_002(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_002] 신청 내용은 10자 이상 1000자 이하여야 합니다."),
    MENTORING_003(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_003] 멘토링 분야는 필수입니다."),

    // ── 멘토링 신청 상태/권한 검증 ────────────────────────────────
    MENTORING_010(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_010] B/C 등급 작업자만 멘토링을 신청할 수 있습니다."),
    MENTORING_011(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_011] 같은 분야에 진행 중인 멘토링 신청이 이미 존재합니다."),
    MENTORING_012(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_012] 본인이 신청한 요청만 수정할 수 있습니다."),
    MENTORING_013(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_013] PENDING 상태의 신청만 수정할 수 있습니다."),
    MENTORING_014(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_014] 이미 처리된 멘토링 신청입니다."),
    MENTORING_015(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_015] 본인 신청을 본인이 수락할 수 없습니다."),

    // ── 멘토 권한/분야 검증 ───────────────────────────────────────
    MENTORING_020(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_020] 해당 분야의 멘토 자격이 없습니다."),
    MENTORING_021(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_021] S/A 등급 작업자, 팀장, 부서장만 멘토로 참여할 수 있습니다."),
    MENTORING_022(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_022] 이미 거절한 멘토링 신청입니다."),

    // ── 멘토링 진행 검증 ─────────────────────────────────────────
    MENTORING_030(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_030] 담당 멘토만 완료 처리할 수 있습니다."),
    MENTORING_031(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_031] 진행 중인 멘토링만 완료 처리할 수 있습니다."),

    // ── 리소스 없음 ───────────────────────────────────────────────
    MENTORING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_REQUEST_NOT_FOUND] 멘토링 신청을 찾을 수 없습니다."),
    MENTORING_EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_EMPLOYEE_NOT_FOUND] 멘토링 대상 직원을 찾을 수 없습니다."),
    MENTORING_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_NOT_FOUND] 진행 중인 멘토링을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MentoringErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
