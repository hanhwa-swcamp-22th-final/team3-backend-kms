package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTagId;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgetag.KnowledgeTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleTagRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeTagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class KnowledgeArticleMyQueryControllerIntegrationTest {

    // 운영 데이터와 충돌하지 않도록 통합 테스트 전용 작성자/태그 ID를 사용한다.
    private static final Long AUTHOR_ID = 1990000000001L;
    private static final Long OTHER_AUTHOR_ID = 1990000000002L;
    private static final Long DEPARTMENT_ID = 1774590584056408L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    private static final Long TAG_ID = 9900000000001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;

    @Autowired
    private KnowledgeArticleTagRepository knowledgeArticleTagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 insert 시 FK 제약에 막히지 않도록 필요한 최소 참조 데이터를 먼저 보장한다.
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        // 내 글 조회 대상 작성자
        jdbcTemplate.execute(
                "INSERT IGNORE INTO employee " +
                        "(employee_id, department_id, employee_code, employee_name, employee_email, employee_phone, employee_address, " +
                        "employee_emergency_contact, employee_password, employee_role, employee_status, employee_tier, hire_date, " +
                        "mfa_enabled, login_fail_count, is_locked) " +
                        "VALUES (" + AUTHOR_ID + ", " + DEPARTMENT_ID + ", 'ITESTMY0001', '내지식통합테스트작성자', " +
                        "'it-my-query-1@example.com', '010-0000-0001', '테스트주소1', '010-9999-0001', " +
                        "'$2b$10$E7KQL8oi/hSAOxTY448oHe9yajHPX9uR79xT2Kazk/5d93lhLyWFG', 'WORKER', 'ACTIVE', 'A', " +
                        "'2026-01-01', 0, 0, 0)"
        );
        // 결과에서 제외되어야 하는 다른 작성자
        jdbcTemplate.execute(
                "INSERT IGNORE INTO employee " +
                        "(employee_id, department_id, employee_code, employee_name, employee_email, employee_phone, employee_address, " +
                        "employee_emergency_contact, employee_password, employee_role, employee_status, employee_tier, hire_date, " +
                        "mfa_enabled, login_fail_count, is_locked) " +
                        "VALUES (" + OTHER_AUTHOR_ID + ", " + DEPARTMENT_ID + ", 'ITESTMY0002', '다른작성자', " +
                        "'it-my-query-2@example.com', '010-0000-0002', '테스트주소2', '010-9999-0002', " +
                        "'$2b$10$E7KQL8oi/hSAOxTY448oHe9yajHPX9uR79xT2Kazk/5d93lhLyWFG', 'WORKER', 'ACTIVE', 'A', " +
                        "'2026-01-01', 0, 0, 0)"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @Test
    @DisplayName("GET /api/kms/my/articles/stats integration success: return my article counts only")
    void getMyArticleStats_success() throws Exception {
        // 내 문서는 상태별로 1건씩 만들고, 삭제 문서와 타 작성자 문서는 집계에서 제외되는지 확인한다.
        saveArticle(AUTHOR_ID, "승인 완료 문서", ArticleStatus.APPROVED, false, LocalDateTime.now().minusDays(3));
        saveArticle(AUTHOR_ID, "승인 대기 문서", ArticleStatus.PENDING, false, LocalDateTime.now().minusDays(2));
        saveArticle(AUTHOR_ID, "반려 문서", ArticleStatus.REJECTED, false, LocalDateTime.now().minusDays(1));
        saveArticle(AUTHOR_ID, "임시 저장 문서", ArticleStatus.DRAFT, false, LocalDateTime.now());
        saveArticle(AUTHOR_ID, "삭제된 임시 문서", ArticleStatus.DRAFT, true, LocalDateTime.now());
        saveArticle(OTHER_AUTHOR_ID, "다른 작성자 문서", ArticleStatus.APPROVED, false, LocalDateTime.now());
        flushAndClear();

        mockMvc.perform(get("/api/kms/my/articles/stats")
                        .param("authorId", String.valueOf(AUTHOR_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvedCount").value(1))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.rejectedCount").value(1))
                .andExpect(jsonPath("$.data.draftCount").value(1))
                .andExpect(jsonPath("$.data.total").value(4));
    }

    @Test
    @DisplayName("GET /api/kms/my/articles integration success: return title and tags for my article only")
    void getMyArticles_success() throws Exception {
        // 내 문서 1건과 타 작성자 문서 1건을 넣은 뒤, 내 문서만 조회되고 태그까지 응답에 포함되는지 본다.
        KnowledgeArticle myArticle = saveArticle(
                AUTHOR_ID,
                "내 지식 문서 제목입니다",
                ArticleStatus.APPROVED,
                false,
                LocalDateTime.now()
        );
        saveArticle(OTHER_AUTHOR_ID, "다른 작성자 문서", ArticleStatus.APPROVED, false, LocalDateTime.now().minusHours(1));

        // 목록 조회 후 서비스가 태그를 문서별로 다시 붙여주는 흐름을 함께 검증한다.
        knowledgeTagRepository.save(KnowledgeTag.builder()
                .tagId(TAG_ID)
                .tagName("통합태그")
                .build());
        knowledgeArticleTagRepository.save(KnowledgeArticleTag.builder()
                .id(new KnowledgeArticleTagId(TAG_ID, myArticle.getArticleId()))
                .build());
        flushAndClear();

        mockMvc.perform(get("/api/kms/my/articles")
                        .param("authorId", String.valueOf(AUTHOR_ID))
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].articleTitle").value("내 지식 문서 제목입니다"))
                .andExpect(jsonPath("$.data[0].title").value("내 지식 문서 제목입니다"))
                .andExpect(jsonPath("$.data[0].tags[0].tagName").value("통합태그"));
    }

    @Test
    @DisplayName("GET /api/kms/my/articles/history integration success: return recent three updated articles")
    void getMyRecentArticleHistory_success() throws Exception {
        // 엔티티 저장 시 createdAt/updatedAt가 현재 시각으로 덮일 수 있어, 저장 후 DB 값을 직접 보정한다.
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 9, 10, 0);

        // 내 문서 4건 중 updated_at 기준 최신 3건만 내려오고, 타 작성자 문서는 제외되어야 한다.
        KnowledgeArticle oldestArticle = saveArticle(AUTHOR_ID, "오래된 문서", ArticleStatus.PENDING, false, baseTime.minusHours(3));
        KnowledgeArticle secondArticle = saveArticle(AUTHOR_ID, "두 번째 문서", ArticleStatus.PENDING, false, baseTime.minusHours(2));
        KnowledgeArticle thirdArticle = saveArticle(AUTHOR_ID, "세 번째 문서", ArticleStatus.REJECTED, false, baseTime.minusHours(1));
        KnowledgeArticle latestArticle = saveArticle(AUTHOR_ID, "가장 최근 문서", ArticleStatus.DRAFT, false, baseTime);
        KnowledgeArticle otherAuthorArticle = saveArticle(OTHER_AUTHOR_ID, "다른 작성자 최근 문서", ArticleStatus.PENDING, false, baseTime.plusHours(1));

        // insert를 먼저 DB에 반영한 뒤, DB 시간을 직접 보정해야 이후 flush에서 다시 덮이지 않는다.
        entityManager.flush();
        updateArticleTimestamps(oldestArticle.getArticleId(), baseTime.minusHours(3));
        updateArticleTimestamps(secondArticle.getArticleId(), baseTime.minusHours(2));
        updateArticleTimestamps(thirdArticle.getArticleId(), baseTime.minusHours(1));
        updateArticleTimestamps(latestArticle.getArticleId(), baseTime);
        updateArticleTimestamps(otherAuthorArticle.getArticleId(), baseTime.plusHours(1));
        entityManager.clear();

        mockMvc.perform(get("/api/kms/my/articles/history")
                        .param("authorId", String.valueOf(AUTHOR_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value("가장 최근 문서"))
                .andExpect(jsonPath("$.data[1].title").value("세 번째 문서"))
                .andExpect(jsonPath("$.data[2].title").value("두 번째 문서"));
    }

    private KnowledgeArticle saveArticle(Long authorId, String title, ArticleStatus status, boolean deleted, LocalDateTime dateTime) {
        // 내 지식 관리 조회에 필요한 최소 필드만 채워 테스트용 문서를 저장한다.
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(authorId)
                .equipmentId(EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(title)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("통합 테스트용 본문입니다. 내 지식 관리 조회 검증을 위해 충분한 길이의 본문을 넣습니다.")
                .articleStatus(status)
                .approvalVersion(status == ArticleStatus.APPROVED ? 1 : 0)
                .isDeleted(deleted)
                .viewCount(0)
                .createdAt(dateTime)
                .updatedAt(dateTime)
                .build());
    }

    private void updateArticleTimestamps(Long articleId, LocalDateTime dateTime) {
        jdbcTemplate.update(
                "UPDATE knowledge_article SET created_at = ?, updated_at = ? WHERE article_id = ?",
                dateTime,
                dateTime,
                articleId
        );
    }

    private void flushAndClear() {
        // 영속성 컨텍스트를 비워 실제 DB 조회 결과 기준으로 검증한다.
        entityManager.flush();
        entityManager.clear();
    }
}
