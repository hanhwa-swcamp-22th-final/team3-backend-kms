package com.ohgiraffers.team3backendkms.kms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("db")
@DisplayName("KnowledgeArticle Integration Test")
public class KnowledgeArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    private final Long validAuthorId = 1774942559890303L;
    private final Long TEST_EQUIPMENT_ID = 1774836457838985L;
    private final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 룰루랄라 룰루랄라 충분한 길이 확보.";

    private UserDetails workerUser;
    private UserDetails tlUser;
    private UserDetails adminUser;

    @BeforeEach
    void setUp() {
        workerUser = new User(validAuthorId.toString(), "", Collections.singleton(new SimpleGrantedAuthority("ROLE_WORKER")));
        tlUser = new User("20", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_TEAM_LEADER")));
        adminUser = new User("admin", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // =========================================================
    // POST /api/kms/articles
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles")
    class Register {

        @Test
        @DisplayName("Returns 201 Created and saves article with PENDING status")
        void register_savesArticleWithPendingStatus() throws Exception {
            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "equipmentId", TEST_EQUIPMENT_ID,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // =========================================================
    // POST /api/kms/tl/approval/{articleId}/approve
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/tl/approval/{articleId}/approve")
    class Approve {

        @Test
        @DisplayName("Returns 200 OK and changes status to APPROVED in DB")
        void approve_statusChangedToApproved() throws Exception {
            KnowledgeArticle saved = savePendingArticle();

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/approve")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("approverId", 20L, "reviewComment", "최종 승인합니다."))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertEquals(ArticleStatus.APPROVED, updated.getArticleStatus());
        }
    }

    // =========================================================
    // DELETE /api/kms/admin/articles/{articleId}
    // =========================================================

    @Nested
    @DisplayName("DELETE /api/kms/admin/articles/{articleId}")
    class AdminDelete {

        @Test
        @DisplayName("Admin can delete any article with a reason")
        void adminDelete_success() throws Exception {
            KnowledgeArticle saved = saveApprovedArticle();
            String reason = "규정 위반 문서 강제 삭제 (10자 이상 필수)";

            mockMvc.perform(delete("/api/kms/admin/articles/" + saved.getArticleId())
                            .with(user(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("deletionReason", reason))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertTrue(updated.getIsDeleted());
            assertEquals(reason, updated.getArticleDeletionReason());
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private KnowledgeArticle savePendingArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(false)
                .build());
    }

    private KnowledgeArticle saveApprovedArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.APPROVED)
                .isDeleted(false)
                .build());
    }
}
