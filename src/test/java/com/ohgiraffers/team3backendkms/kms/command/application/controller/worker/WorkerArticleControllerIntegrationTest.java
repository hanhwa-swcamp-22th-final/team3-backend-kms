package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class WorkerArticleControllerIntegrationTest {

    private static final String BASE_URL = "/api/kms/articles";
    private static final Long AUTHOR_ID = 1774942559890303L;
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    private static final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private static final String UPDATED_TITLE = "수정된 통합 테스트 제목입니다";
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";
    private static final String UPDATED_CONTENT = "수정된 통합 테스트 본문입니다. 이 본문도 최소 50자 이상이어야 하며 상태 전환까지 검증합니다.";

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

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
            "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @Test
    @DisplayName("Create article API integration success: persist article with pending status")
    void createArticle_success() throws Exception {
        // given
        Map<String, Object> request = Map.of(
            "authorId", AUTHOR_ID,
            "equipmentId", EQUIPMENT_ID,
            "title", TITLE,
            "category", "TROUBLESHOOTING",
            "content", CONTENT
        );

        // when
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        // then
        Long articleId = extractArticleId(result);
        KnowledgeArticle savedArticle = knowledgeArticleRepository.findById(articleId).orElseThrow();

        assertEquals(ArticleStatus.PENDING, savedArticle.getArticleStatus());
        assertEquals(TITLE, savedArticle.getArticleTitle());
        assertEquals(AUTHOR_ID, savedArticle.getAuthorId());
    }

    @Test
    @DisplayName("Update draft article API integration success: update content and change status to pending")
    void updateDraftArticle_success() throws Exception {
        // given
        KnowledgeArticle draftArticle = saveArticle(ArticleStatus.DRAFT, TITLE, CONTENT, 0);
        Map<String, Object> request = Map.of(
            "authorId", AUTHOR_ID,
            "title", UPDATED_TITLE,
            "category", "PROCESS_IMPROVEMENT",
            "content", UPDATED_CONTENT
        );

        // when
        mockMvc.perform(put(BASE_URL + "/{articleId}", draftArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        flushAndClear();

        // then
        KnowledgeArticle updatedArticle = knowledgeArticleRepository.findById(draftArticle.getArticleId()).orElseThrow();
        assertEquals(UPDATED_TITLE, updatedArticle.getArticleTitle());
        assertEquals(UPDATED_CONTENT, updatedArticle.getArticleContent());
        assertEquals(ArticleStatus.PENDING, updatedArticle.getArticleStatus());
    }

    private Long extractArticleId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("data").asLong();
    }

    private KnowledgeArticle saveArticle(ArticleStatus status, String title, String content, int viewCount) {
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
            .viewCount(viewCount)
            .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
