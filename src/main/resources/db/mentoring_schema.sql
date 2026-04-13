-- ── 멘토링 분야 매핑 테이블 ──────────────────────────────────────────
-- Admin이 직접 관리 (S/A Worker, TL, DL 에게만 등록 가능)
CREATE TABLE IF NOT EXISTS employee_mentoring_field (
    employee_id     BIGINT       NOT NULL,
    mentoring_field VARCHAR(50)  NOT NULL COMMENT 'ArticleCategory enum (TROUBLESHOOTING, PROCESS_IMPROVEMENT, EQUIPMENT_OPERATION, SAFETY, ETC)',
    PRIMARY KEY (employee_id, mentoring_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 멘토링 신청 테이블 ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mentoring_request (
    request_id          BIGINT          NOT NULL AUTO_INCREMENT,
    mentee_id           BIGINT          NOT NULL,
    mentor_id           BIGINT          NULL COMMENT '수락 전 NULL, 수락 후 확정',
    article_id          BIGINT          NULL COMMENT '선택적 참조 문서',
    mentoring_field     VARCHAR(50)     NOT NULL COMMENT 'ArticleCategory enum',
    request_title       VARCHAR(50)     NOT NULL,
    request_content     VARCHAR(1000)   NOT NULL,
    request_status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACCEPTED / REJECTED',
    rejected_mentor_ids TEXT            NULL COMMENT '거절한 멘토 ID 목록 (JSON 배열)',
    requested_at        DATETIME        NULL,
    processed_at        DATETIME        NULL,
    PRIMARY KEY (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 진행중 멘토링 테이블 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mentoring (
    mentoring_id     BIGINT   NOT NULL AUTO_INCREMENT,
    request_id       BIGINT   NOT NULL UNIQUE COMMENT 'mentoring_request.request_id 참조',
    mentor_id        BIGINT   NOT NULL,
    mentee_id        BIGINT   NOT NULL,
    mentoring_status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / COMPLETED',
    started_at       DATETIME NOT NULL,
    completed_at     DATETIME NULL,
    PRIMARY KEY (mentoring_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
