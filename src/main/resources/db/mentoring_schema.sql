-- ── 멘토 자격 분야 테이블 ─────────────────────────────────────────────
-- Admin이 직접 관리 (S/A Worker, TL, DL 에게만 등록 가능)
CREATE TABLE IF NOT EXISTS employee_mentoring_field (
    employee_mentoring_field_id BIGINT          NOT NULL AUTO_INCREMENT,
    employee_id                 BIGINT          NOT NULL,
    mentoring_field             VARCHAR(100)    NOT NULL,
    created_at                  TIMESTAMP       NULL,
    created_by                  BIGINT          NULL,
    updated_at                  TIMESTAMP       NULL,
    updated_by                  BIGINT          NULL,
    PRIMARY KEY (employee_mentoring_field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 멘토링 신청 테이블 ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mentoring_request (
    request_id                  BIGINT          NOT NULL AUTO_INCREMENT,
    article_id                  BIGINT          NULL     COMMENT '선택적 참조 문서',
    mentor_id                   BIGINT          NULL     COMMENT '수락 전 NULL, 수락 후 확정',
    mentee_id                   BIGINT          NOT NULL,
    mentoring_field             VARCHAR(100)    NOT NULL,
    request_title               VARCHAR(255)    NOT NULL,
    request_content             VARCHAR(1000)   NOT NULL,
    mentoring_duration_weeks    INT             NULL,
    mentoring_frequency         VARCHAR(50)     NULL,
    request_priority            ENUM('HIGH', 'MEDIUM', 'LOW') NULL,
    request_status              ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reject_reason               VARCHAR(500)    NULL,
    rejected_mentor_ids         JSON            NULL     COMMENT '개인 거절한 멘토 ID 목록',
    created_at                  TIMESTAMP       NULL,
    created_by                  BIGINT          NULL,
    updated_at                  TIMESTAMP       NULL,
    updated_by                  BIGINT          NULL,
    PRIMARY KEY (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 진행중/완료 멘토링 테이블 ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mentoring (
    mentoring_id        BIGINT      NOT NULL AUTO_INCREMENT,
    request_id          BIGINT      NOT NULL UNIQUE COMMENT 'mentoring_request.request_id 참조',
    mentor_id           BIGINT      NOT NULL,
    mentee_id           BIGINT      NOT NULL,
    mentoring_status    ENUM('IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'IN_PROGRESS',
    created_at          TIMESTAMP   NULL     COMMENT '멘토링 시작 시각 (수락 시점)',
    created_by          BIGINT      NULL,
    updated_at          TIMESTAMP   NULL     COMMENT '마지막 업데이트 (완료 시점)',
    updated_by          BIGINT      NULL,
    PRIMARY KEY (mentoring_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
