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

        @Test
        @DisplayName("Failure: Invalid equipment ID returns 400 (ARTICLE_005)")
        void register_invalidEquipmentId_fails() throws Exception {
            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "equipmentId", -1L,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_005] 유효하지 않은 설비 ID입니다."));
        }
    }

    // =========================================================
    // POST /api/kms/articles/drafts (임시저장)
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/articles/drafts")
    class Draft {

        @Test
        @DisplayName("Success: Returns 201 Created and saves article with DRAFT status")
        void draft_savesArticleWithDraftStatus() throws Exception {
            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "equipmentId", TEST_EQUIPMENT_ID,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(post("/api/kms/articles/drafts")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle saved = knowledgeArticleRepository.findAll().stream()
                    .max((a, b) -> a.getArticleId().compareTo(b.getArticleId()))
                    .orElseThrow();
            assertEquals(ArticleStatus.DRAFT, saved.getArticleStatus());
        }

        @Test
        @DisplayName("Failure: Invalid equipment ID returns 400 (ARTICLE_005)")
        void draft_invalidEquipmentId_fails() throws Exception {
            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "equipmentId", -1L,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(post("/api/kms/articles/drafts")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_005] 유효하지 않은 설비 ID입니다."));
        }
    }

    // =========================================================
    // PUT /api/kms/articles/{articleId} (수정)
    // =========================================================

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}")
    class Update {

        @Test
        @DisplayName("Success: Updates article and changes status to PENDING")
        void update_updatesArticleAndChangeStatusToPending() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            String updatedTitle = "수정된 제목입니다 (5자 이상 200자 이하)";
            String updatedContent = "수정된 본문 내용입니다. 이 본문은 최소 50자 이상이어야 합니다. 충분한 길이 확보됩니다.";

            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "title", updatedTitle,
                    "category", "PROCESS_IMPROVEMENT",
                    "content", updatedContent
            );

            mockMvc.perform(put("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertEquals(updatedTitle, updated.getArticleTitle());
            assertEquals(updatedContent, updated.getArticleContent());
            assertEquals(ArticleStatus.PENDING, updated.getArticleStatus());
        }

        @Test
        @DisplayName("Failure: DRAFT가 아닌 상태에서 수정 불가 (ARTICLE_006)")
        void update_nonDraftArticle_fails() throws Exception {
            KnowledgeArticle saved = savePendingArticle();

            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(put("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_006] DRAFT 상태에서만 수정할 수 있습니다."));
        }

        @Test
        @DisplayName("Failure: 다른 사용자가 수정 시도 (ARTICLE_007)")
        void update_otherUserArticle_fails() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            Long differentUserId = 9999999L;

            Map<String, Object> request = Map.of(
                    "authorId", differentUserId,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            mockMvc.perform(put("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."));
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
    // POST /api/kms/tl/approval/{articleId}/reject (반려)
    // =========================================================

    @Nested
    @DisplayName("POST /api/kms/tl/approval/{articleId}/reject")
    class TLReject {

        @Test
        @DisplayName("Success: Changes status to REJECTED and saves reason in DB")
        void reject_statusChangedToRejected() throws Exception {
            KnowledgeArticle saved = savePendingArticle();
            String reason = "내용이 부족합니다. 보충 후 재제출해주세요.";

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("reviewComment", reason))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertEquals(ArticleStatus.REJECTED, updated.getArticleStatus());
            assertEquals(reason, updated.getArticleRejectionReason());
        }

        @Test
        @DisplayName("Failure: 반려 사유 길이 미충족 (APPROVAL_001)")
        void reject_invalidReasonLength_fails() throws Exception {
            KnowledgeArticle saved = savePendingArticle();

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("rejecterId", 20L, "reason", "짧음"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[APPROVAL_001] 반려 사유는 10자 이상 500자 이하여야 합니다."));
        }

        @Test
        @DisplayName("Failure: PENDING이 아닌 상태에서 반려 불가 (APPROVAL_003)")
        void reject_nonPendingArticle_fails() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            String reason = "이 문서는 승인 대기 상태가 아닙니다.";

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("reviewComment", reason))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."));
        }
    }

    // =========================================================
    // GET /api/kms/articles/{articleId} (상세조회)
    // =========================================================

    @Nested
    @DisplayName("GET /api/kms/articles/{articleId}")
    class GetDetail {

        @Test
        @DisplayName("Success: Returns 200 OK with article detail and increments view count")
        void getDetail_returnsArticleAndIncrementsViewCount() throws Exception {
            KnowledgeArticle saved = savePendingArticle();
            int initialViewCount = saved.getViewCount();

            mockMvc.perform(get("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleTitle").value(TITLE));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertEquals(initialViewCount + 1, updated.getViewCount(), "조회수가 1 증가해야 함");
        }

        @Test
        @DisplayName("Failure: Non-existent article returns 404 (NOT_FOUND)")
        void getDetail_nonExistentArticle_fails() throws Exception {
            Long nonExistentId = 9999999999999L;

            mockMvc.perform(get("/api/kms/articles/" + nonExistentId)
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE] 문서를 찾을 수 없습니다."));
        }
    }

    // =========================================================
    // DELETE /api/kms/articles/{articleId}
    // =========================================================

    @Nested
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class WorkerDelete {

        @Test
        @DisplayName("Success: DRAFT status article can be deleted")
        void delete_draftArticle_success() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertTrue(updated.getIsDeleted());
        }

        @Test
        @DisplayName("Failure: APPROVED status article cannot be deleted (ARTICLE_009)")
        void delete_approvedArticle_fails() throws Exception {
            KnowledgeArticle saved = saveApprovedArticle();

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."));

            KnowledgeArticle unchanged = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertFalse(unchanged.getIsDeleted());
        }

        @Test
        @DisplayName("Failure: PENDING status article cannot be deleted (ARTICLE_010)")
        void delete_pendingArticle_fails() throws Exception {
            KnowledgeArticle saved = savePendingArticle();

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."));

            KnowledgeArticle unchanged = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertFalse(unchanged.getIsDeleted());
        }

        @Test
        @DisplayName("Failure: REJECTED status article cannot be deleted (ARTICLE_010)")
        void delete_rejectedArticle_fails() throws Exception {
            KnowledgeArticle saved = saveRejectedArticle();

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."));

            KnowledgeArticle unchanged = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertFalse(unchanged.getIsDeleted());
        }

        @Test
        @DisplayName("Failure: Already deleted article cannot be deleted again (ARTICLE_008)")
        void delete_alreadyDeletedArticle_fails() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            saved.softDelete();
            knowledgeArticleRepository.save(saved);

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_008] 이미 삭제된 문서입니다."));
        }

        @Test
        @DisplayName("Failure: Other user cannot delete someone else's article (ARTICLE_007)")
        void delete_otherUserArticle_fails() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            Long differentUserId = 9999999L;

            mockMvc.perform(delete("/api/kms/articles/" + saved.getArticleId())
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", differentUserId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE_007] 본인이 작성한 문서만 삭제할 수 있습니다."));

            KnowledgeArticle unchanged = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertFalse(unchanged.getIsDeleted());
        }

        @Test
        @DisplayName("Failure: Non-existent article cannot be deleted (NOT_FOUND)")
        void delete_nonExistentArticle_fails() throws Exception {
            Long nonExistentId = 9999999999999L;

            mockMvc.perform(delete("/api/kms/articles/" + nonExistentId)
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("requesterId", validAuthorId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("[ARTICLE] 문서를 찾을 수 없습니다."));
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
                .viewCount(0)
                .build());
    }

    private KnowledgeArticle saveDraftArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .viewCount(0)
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
                .viewCount(0)
                .build());
    }

    private KnowledgeArticle saveRejectedArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.REJECTED)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }
}
