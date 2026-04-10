package com.ohgiraffers.team3backendkms.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MentoringErrorCode implements ErrorCode {

    MENTORING_REQUEST_001(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_001] 멘토링 신청은 B/C 등급 작업자만 가능합니다."),
    MENTORING_REQUEST_002(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_002] 재직 중인 직원만 멘토링을 신청할 수 있습니다."),
    MENTORING_REQUEST_003(HttpStatus.CONFLICT, "CONFLICT", "[MENTORING_REQUEST_003] 동일한 분야의 진행 중인 멘토링 신청이 이미 존재합니다."),
    MENTORING_REQUEST_004(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_REQUEST_004] 멘토링 신청자를 찾을 수 없습니다."),
    MENTORING_REQUEST_005(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_REQUEST_005] 연결된 문서를 찾을 수 없습니다."),
    MENTORING_REQUEST_006(HttpStatus.NOT_FOUND, "NOT_FOUND", "[MENTORING_REQUEST_006] 멘토링 요청을 찾을 수 없습니다."),
    MENTORING_REQUEST_007(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_007] PENDING 상태의 멘토링 요청만 수락할 수 있습니다."),
    MENTORING_REQUEST_008(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_008] 해당 요청을 수락할 수 있는 멘토 권한이 없습니다."),
    MENTORING_REQUEST_009(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_009] 본인이 등록한 멘토링 요청은 직접 수락할 수 없습니다."),
    MENTORING_REQUEST_010(HttpStatus.CONFLICT, "CONFLICT", "[MENTORING_REQUEST_010] 이미 수락되어 진행 중인 멘토링 요청입니다."),
    MENTORING_REQUEST_011(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "[MENTORING_REQUEST_011] 멘토링 요청에는 연결 문서가 필수입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MentoringErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
