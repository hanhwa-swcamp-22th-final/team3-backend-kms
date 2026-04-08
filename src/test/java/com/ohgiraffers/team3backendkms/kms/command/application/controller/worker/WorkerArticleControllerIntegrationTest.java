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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("db")
class WorkerArticleControllerIntegrationTest {

    // Worker 문서 API 공통 경로다.
    private static final String BASE_URL = "/api/kms/articles";
    // 테스트에서 사용할 작성자 ID다.
    private static final Long AUTHOR_ID = 1774942559890303L;
    // 테스트에서 사용할 설비 ID다.
    private static final Long EQUIPMENT_ID = 1774836457838985L;
    // 등록 및 수정 전 기본 제목이다.
    private static final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    // 수정 후 사용할 제목이다.
    private static final String UPDATED_TITLE = "수정된 통합 테스트 제목입니다";
    // 등록용 본문이다.
    private static final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 충분한 길이를 확보했습니다.";
    // 제출 테스트에서 사용할 수정 본문이다.
    private static final String UPDATED_CONTENT = "수정된 통합 테스트 본문입니다. 이 본문도 최소 50자 이상이어야 하며 상태 전환까지 검증합니다.";

    // 실제 HTTP 요청처럼 컨트롤러를 호출하기 위한 도구다.
    @Autowired
    private MockMvc mockMvc;

    // 요청/응답 JSON 변환에 사용한다.
    @Autowired
    private ObjectMapper objectMapper;

    // 저장된 엔티티를 다시 조회해 검증할 때 사용한다.
    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    // flush, clear로 JPA 1차 캐시를 비울 때 사용한다.
    @Autowired
    private EntityManager entityManager;

    // 보조 테이블 초기 데이터를 직접 넣을 때 사용한다.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 문서 저장에 필요한 보조 데이터를 테스트 전에 맞춰 둔다.
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
        // 등록 요청 본문을 JSON 형태로 만들기 위해 Map으로 준비한다.
        Map<String, Object> request = Map.of(
            "authorId", AUTHOR_ID,
            "equipmentId", EQUIPMENT_ID,
            "title", TITLE,
            "category", "TROUBLESHOOTING",
            "content", CONTENT
        );

        // when
        // 등록 API를 실제로 호출하고 응답 결과를 받아 둔다.
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        // then
        // 응답에서 생성된 articleId를 꺼내 DB에 저장된 상태를 직접 검증한다.
        Long articleId = extractArticleId(result);
        KnowledgeArticle savedArticle = knowledgeArticleRepository.findById(articleId).orElseThrow();

        // 등록 문서는 PENDING 상태로 저장되어야 한다.
        assertEquals(ArticleStatus.PENDING, savedArticle.getArticleStatus());
        // 제목이 요청값과 같아야 한다.
        assertEquals(TITLE, savedArticle.getArticleTitle());
        // 작성자 ID가 요청값과 같아야 한다.
        assertEquals(AUTHOR_ID, savedArticle.getAuthorId());
    }

    @Test
    @DisplayName("Update draft article API integration success: update content and keep status as draft")
    void updateDraftArticle_success() throws Exception {
        // given
        // 수정 대상이 될 DRAFT 문서를 먼저 저장한다.
        KnowledgeArticle draftArticle = saveArticle(ArticleStatus.DRAFT, TITLE, CONTENT, 0);
        // 수정 요청 본문을 준비한다.
        Map<String, Object> request = Map.of(
            "authorId", AUTHOR_ID,
            "equipmentId", EQUIPMENT_ID,
            "title", UPDATED_TITLE,
            "category", "PROCESS_IMPROVEMENT",
            "content", "임시저장 중인 수정 본문입니다."
        );

        // when
        // 임시저장 수정 API를 호출한다.
        mockMvc.perform(put(BASE_URL + "/{articleId}", draftArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // DB에 반영된 최신 값으로 다시 조회할 수 있게 한다.
        flushAndClear();

        // then
        // 수정 후 제목과 본문은 바뀌고 상태는 DRAFT로 유지되어야 한다.
        KnowledgeArticle updatedArticle = knowledgeArticleRepository.findById(draftArticle.getArticleId()).orElseThrow();
        assertEquals(UPDATED_TITLE, updatedArticle.getArticleTitle());
        assertEquals("임시저장 중인 수정 본문입니다.", updatedArticle.getArticleContent());
        assertEquals(ArticleStatus.DRAFT, updatedArticle.getArticleStatus());
    }

    @Test
    @DisplayName("Submit draft article API integration success: update content and change status to pending")
    void submitDraftArticle_success() throws Exception {
        // given
        // 제출 대상이 될 DRAFT 문서를 먼저 저장한다.
        KnowledgeArticle draftArticle = saveArticle(ArticleStatus.DRAFT, TITLE, "임시 작성 중인 본문", 0);
        // 제출 요청 본문을 준비한다.
        Map<String, Object> request = Map.of(
            "authorId", AUTHOR_ID,
            "equipmentId", EQUIPMENT_ID,
            "title", UPDATED_TITLE,
            "category", "PROCESS_IMPROVEMENT",
            "content", UPDATED_CONTENT
        );

        // when
        // 제출 API를 호출해 상태 전이를 유도한다.
        mockMvc.perform(put(BASE_URL + "/{articleId}/submit", draftArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // 캐시 대신 DB 기준으로 다시 검증하기 위해 비운다.
        flushAndClear();

        // then
        // 제출 후 제목/본문은 수정값으로 바뀌고 상태는 PENDING이어야 한다.
        KnowledgeArticle updatedArticle = knowledgeArticleRepository.findById(draftArticle.getArticleId()).orElseThrow();
        assertEquals(UPDATED_TITLE, updatedArticle.getArticleTitle());
        assertEquals(UPDATED_CONTENT, updatedArticle.getArticleContent());
        assertEquals(ArticleStatus.PENDING, updatedArticle.getArticleStatus());
    }

    @Test
    @DisplayName("Start revision API integration success: save edit history and change approved article to draft")
    void startRevision_success() throws Exception {
        // given
        KnowledgeArticle approvedArticle = saveArticle(ArticleStatus.APPROVED, TITLE, CONTENT, 0);
        Map<String, Object> request = Map.of(
            "requesterId", AUTHOR_ID
        );

        // when
        mockMvc.perform(put(BASE_URL + "/{articleId}/revision", approvedArticle.getArticleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        flushAndClear();

        // then
        KnowledgeArticle revisedArticle = knowledgeArticleRepository.findById(approvedArticle.getArticleId()).orElseThrow();
        assertEquals(ArticleStatus.DRAFT, revisedArticle.getArticleStatus());
        assertEquals(1, revisedArticle.getApprovalVersion());

        Map<String, Object> history = jdbcTemplate.queryForMap(
            "SELECT * FROM knowledge_edit_history WHERE article_id = ? AND approval_version = ?",
            approvedArticle.getArticleId(),
            1
        );

        assertNotNull(history.get("history_id"));
        assertEquals(AUTHOR_ID, ((Number) history.get("editor_id")).longValue());
        assertEquals(TITLE, history.get("article_title"));
        assertEquals(CONTENT, history.get("article_content"));
    }

    private Long extractArticleId(MvcResult result) throws Exception {
        // 응답 JSON에서 data 필드를 읽어 생성된 문서 ID를 꺼낸다.
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("data").asLong();
    }

    private KnowledgeArticle saveArticle(ArticleStatus status, String title, String content, int viewCount) {
        // 테스트에서 공통으로 사용할 문서를 직접 저장한다.
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
            .articleId(new TimeBasedIdGenerator().generate())
            .authorId(AUTHOR_ID)
            .equipmentId(EQUIPMENT_ID)
            .fileGroupId(0L)
            .articleTitle(title)
            .articleCategory(ArticleCategory.TROUBLESHOOTING)
            .articleContent(content)
            .articleStatus(status)
            .approvalVersion(status == ArticleStatus.APPROVED ? 1 : 0)
            .isDeleted(false)
            .viewCount(viewCount)
            .build());
    }

    private void flushAndClear() {
        // JPA 변경사항을 DB에 반영하고 1차 캐시를 비운다.
        entityManager.flush();
        entityManager.clear();
    }
}
