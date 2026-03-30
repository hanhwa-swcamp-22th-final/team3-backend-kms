package com.ohgiraffers.team3backendkms.kms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.config.security.CustomUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleDraftRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.ArticleRegisterRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    // =========================================================
    // POST /api/kms/articles
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles — 지식 문서 등록")
    class Register {

        @Test
        @DisplayName("정상 요청 시 200 OK 응답과 함께 DB에 PENDING 상태로 저장된다")
        void register_savedAsPending() throws Exception {
            // given
            ArticleRegisterRequest request = new ArticleRegisterRequest(
                    validAuthorId, TITLE, ArticleCategory.TROUBLESHOOTING, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(ArticleStatus.PENDING, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // POST /api/kms/articles/drafts
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles/drafts — 지식 문서 임시저장")
    class Draft {

        @Test
        @DisplayName("정상 요청 시 200 OK 응답과 함께 DB에 DRAFT 상태로 저장된다")
        void draft_savedAsDraft() throws Exception {
            // given
            ArticleDraftRequest request = new ArticleDraftRequest(
                    validAuthorId, TITLE, ArticleCategory.PROCESS_IMPROVEMENT, TEST_EQUIPMENT_ID, CONTENT
            );

            // when
            mockMvc.perform(post("/api/kms/articles/drafts")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // then
            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .filter(a -> TITLE.equals(a.getArticleTitle()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
            assertFalse(saved.getIsDeleted());
        }
    }
}
