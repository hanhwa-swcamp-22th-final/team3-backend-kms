package com.ohgiraffers.team3backendkms.kms.command.application.controller.teamleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class TeamLeaderArticleControllerIntegrationTest {

    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    private static final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long approverId;

    @BeforeEach
    void setUp() {
        approverId = jdbcTemplate.queryForObject(
            "SELECT employee_id FROM employee WHERE employee_id <> ? LIMIT 1",
            Long.class,
            AUTHOR_ID
        );
    }

    @Test
    @DisplayName("Approve article API integration success: update article approval fields")
    void approveArticle_success() throws Exception {
        // given
        KnowledgeArticle pendingArticle = saveArticle(ArticleStatus.PENDING, TITLE, CONTENT);
        Map<String, Object> request = Map.of("reviewComment", "검토 완료, 승인합니다.");

        // when
        mockMvc.perform(post("/api/kms/tl/approval/{articleId}/approve", pendingArticle.getArticleId())
                .header("X-Employee-Id", approverId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        flushAndClear();

        // then
        KnowledgeArticle approvedArticle = knowledgeArticleRepository.findById(pendingArticle.getArticleId()).orElseThrow();
        assertEquals(ArticleStatus.APPROVED, approvedArticle.getArticleStatus());
        assertEquals(approverId, approvedArticle.getApprovedBy());
        assertEquals("검토 완료, 승인합니다.", approvedArticle.getArticleApprovalOpinion());
        assertNotNull(approvedArticle.getApprovedAt());
    }

    private KnowledgeArticle saveArticle(ArticleStatus status, String title, String content) {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(AUTHOR_ID)
            .equipmentId(EQUIPMENT_ID)
            .fileGroupId(0L)
            .articleTitle(title)
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent(content)
            .articleStatus(status)
            .isDeleted(false)
            .viewCount(0)
            .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
