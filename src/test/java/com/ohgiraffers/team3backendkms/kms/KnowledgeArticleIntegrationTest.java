package com.ohgiraffers.team3backendkms.kms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.config.security.CustomUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("KnowledgeArticle 전체 통합 테스트 (5-5)")
class KnowledgeArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 테스트용 상수
    private static final String TITLE   = "통합테스트 지식 문서 제목입니다";
    private static final String CONTENT = "통합테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.가나다라마바사";
    private static final Long   TEST_EQUIPMENT_ID = 9000000092L;

    // 인증 사용자 (WORKER — 등록, 임시저장, 삭제)
    protected CustomUserDetails workerUser;

    // 인증 사용자 (TL — 승인, 반려)
    protected CustomUserDetails tlUser;

    private Long validAuthorId;

    @BeforeEach
    void setUpTestData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
            "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
            "INSERT IGNORE INTO equipment " +
            "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
            "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-EQ-INTG2', '전체통합테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        validAuthorId = jdbcTemplate.queryForObject(
            "SELECT employee_id FROM employee LIMIT 1", Long.class);

        workerUser = new CustomUserDetails(
            "WORKER_TEST", "pw",
            List.of(new SimpleGrantedAuthority("WORKER")),
            validAuthorId
        );

        tlUser = new CustomUserDetails(
            "TL_TEST", "pw",
            List.of(new SimpleGrantedAuthority("TL")),
            validAuthorId
        );
    }
}
