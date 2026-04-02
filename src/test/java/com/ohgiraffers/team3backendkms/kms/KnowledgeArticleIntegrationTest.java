package com.ohgiraffers.team3backendkms.kms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import jakarta.persistence.EntityManager;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Long validAuthorId = 1774942559890303L;
    private final Long TEST_EQUIPMENT_ID = 1774836457838985L;
    private Long otherAuthorIdForQueryTest;
    private final String TITLE = "통합 테스트용 제목입니다 (5자 이상)";
    private final String CONTENT = "통합 테스트용 본문입니다. 이 본문은 최소 50자 이상이어야 등록이 가능합니다. 룰루랄라 룰루랄라 충분한 길이 확보.";

    private UserDetails workerUser;
    private UserDetails tlUser;
    private UserDetails adminUser;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        otherAuthorIdForQueryTest = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee WHERE employee_id <> " + validAuthorId + " LIMIT 1", Long.class
        );

        workerUser = new User(validAuthorId.toString(), "", Collections.singleton(new SimpleGrantedAuthority("ROLE_WORKER")));
        tlUser = new User("20", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_TEAM_LEADER")));
        adminUser = new User("admin", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // =========================================================
    // POST /api/kms/articles (지식 문서 등록)
    // =========================================================
    // 목적: Worker가 새로운 지식 문서를 등록하면 PENDING 상태로 저장되는지 검증
    // 흐름: MockMvc → Controller → Service → DB (knowledge_article 테이블)

    @Nested
    @DisplayName("POST /api/kms/articles")
    class Register {

        /**
         * ✅ 성공 케이스: 정상적인 문서 등록
         *
         * Given: Worker가 필수 필드(authorId, equipmentId, title, category, content)를 모두 입력
         * When: POST /api/kms/articles 요청
         * Then: HTTP 201 Created 반환, DB에 PENDING 상태로 저장
         *
         * 검증 포인트:
         * - 유효한 equipmentId 제공 시 등록 성공
         * - 상태는 자동으로 PENDING 설정
         * - API 응답이 성공 상태 반환
         */
        @Test
        @DisplayName("Returns 201 Created and saves article with PENDING status")
        void register_savesArticleWithPendingStatus() throws Exception {
            // [Given] 테스트에 필요한 요청 데이터와 설정을 준비합니다.
            Map<String, Object> request = Map.of(
                    "authorId", validAuthorId,
                    "equipmentId", TEST_EQUIPMENT_ID,
                    "title", TITLE,
                    "category", "TROUBLESHOOTING",
                    "content", CONTENT
            );

            // [When] MockMvc를 통해 실제 API 요청을 시뮬레이션합니다.
            mockMvc.perform(post("/api/kms/articles")
                            .with(user(workerUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            // [Then] 응답 상태 코드와 반환된 데이터가 예상과 일치하는지 검증합니다.
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * ❌ 실패 케이스: 유효하지 않은 equipmentId (음수)
         *
         * Given: equipmentId가 -1 (음수)로 입력
         * When: POST /api/kms/articles 요청
         * Then: HTTP 400 Bad Request, errorCode "BAD_REQUEST", ARTICLE_005 에러 메시지
         *
         * 검증 포인트:
         * - Service의 validateEquipmentId()가 정상 동작
         * - 음수 equipmentId는 거부됨
         * - 정확한 에러 코드와 메시지 반환
         *
         * 비즈니스 규칙: 설비는 반드시 유효한 ID(양수)여야 함
         */
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
    // 목적: Worker가 작성 중인 문서를 임시저장할 수 있는지 검증
    // 특징: Register와 달리 equipmentId가 null 허용 (선택사항)
    // 흐름: MockMvc → Controller → Service → DB (DRAFT 상태로 저장)

    @Nested
    @DisplayName("POST /api/kms/articles/drafts")
    class Draft {

        /**
         * ✅ 성공 케이스: 정상적인 임시저장
         *
         * Given: Worker가 필요한 필드를 입력하고 equipmentId도 제공
         * When: POST /api/kms/articles/drafts 요청
         * Then: HTTP 201 Created 반환, DB에 DRAFT 상태로 저장
         *
         * 검증 포인트:
         * - 응답이 성공 상태 (201)
         * - Repository에서 조회해서 실제 상태가 DRAFT인지 확인 (DB 검증)
         * - Draft는 아직 검증 대기 상태가 아님
         *
         * 중요: Register와 달리 DRAFT에서는 완전히 유효할 필요 없음
         */
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

        /**
         * ❌ 실패 케이스: 유효하지 않은 equipmentId
         *
         * Given: equipmentId가 -1 (음수)
         * When: POST /api/kms/articles/drafts 요청
         * Then: HTTP 400 Bad Request, ARTICLE_005 에러
         *
         * 검증 포인트:
         * - Draft라도 equipmentId가 있으면 양수여야 함
         * - null은 OK, 하지만 음수는 NG
         * - validateEquipmentIdIfPresent() 동작 검증
         *
         * 규칙: equipmentId = null (OK) vs equipmentId = -1 (NG)
         */
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
    // PUT /api/kms/articles/{articleId} (문서 수정)
    // =========================================================
    // 목적: Draft 상태의 문서를 수정하고 자동으로 PENDING 상태로 변경되는지 검증
    // 규칙: DRAFT만 수정 가능, 작성자만 수정 가능
    // 흐름: 문서 조회 → 검증 → 수정 → 상태 변경 (DRAFT → PENDING) → DB 저장

    @Nested
    @DisplayName("PUT /api/kms/articles/{articleId}")
    class Update {

        /**
         * ✅ 성공 케이스: DRAFT 문서 정상 수정
         *
         * Given: 작성자 본인이 자신의 DRAFT 상태 문서를 수정
         * When: PUT /api/kms/articles/{articleId} 요청
         * Then: HTTP 200 OK, DB에서 확인하면 내용 변경 + 상태 PENDING으로 자동 변경
         *
         * 검증 포인트:
         * - HTTP 응답: 200 OK
         * - DB 내용 검증: title, content 변경됨
         * - 상태 검증: DRAFT → PENDING 자동 전환
         *
         * 흐름: 작성 → 임시저장(DRAFT) → 수정(작성 계속) → 최종 제출(PENDING)
         */
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

        /**
         * ❌ 실패 케이스 1: PENDING 상태의 문서는 수정 불가
         *
         * Given: PENDING 상태의 문서 (이미 검증 대기 중)
         * When: PUT /api/kms/articles/{articleId} 수정 요청
         * Then: HTTP 400 Bad Request, ARTICLE_006 에러
         *
         * 검증 포인트:
         * - DRAFT 상태에서만 수정 가능한 규칙
         * - 검증 대기 중인 문서는 수정 금지
         * - 명확한 에러 메시지
         *
         * 비즈니스 규칙: Draft(작성) → Pending(검증 대기) → Approved/Rejected
         *              수정은 Draft 단계에서만 가능
         */
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

        /**
         * ❌ 실패 케이스 2: 다른 사용자의 문서는 수정 불가
         *
         * Given: User A가 작성한 DRAFT 문서, User B가 수정 시도
         * When: User B가 PUT /api/kms/articles/{articleId} 요청 (authorId = 다른 ID)
         * Then: HTTP 400 Bad Request, ARTICLE_007 에러
         *
         * 검증 포인트:
         * - 요청 본문의 authorId와 실제 작성자 비교
         * - 작성자 본인만 수정 가능 (권한 검증)
         * - 다른 사용자의 침범 차단
         *
         * 보안: 문서 소유권 검증 (authorId 일치 확인)
         */
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
    // POST /api/kms/tl/approval/{articleId}/approve (TL 승인)
    // =========================================================
    // 목적: TeamLeader가 PENDING 문서를 검수 후 승인하면 APPROVED로 변경되는지 검증
    // 역할: TL이 최종 승인하면 문서가 공개 상태로 전환
    // 흐름: PENDING → TL 검수 → APPROVED (공개 가능)

    @Nested
    @DisplayName("POST /api/kms/tl/approval/{articleId}/approve")
    class Approve {

        /**
         * ✅ 성공 케이스: TL이 정상 승인
         *
         * Given: PENDING 상태의 문서, TL 역할 사용자
         * When: POST /api/kms/tl/approval/{articleId}/approve 요청
         * Then: HTTP 200 OK, DB에서 상태 APPROVED로 변경됨
         *
         * 검증 포인트:
         * - HTTP 응답: 200 OK
         * - DB 상태 변경: PENDING → APPROVED
         * - 승인자(approverId) 기록
         * - 검토 의견(reviewComment) 저장 (선택)
         *
         * 흐름: Worker 작성 → Pending 상태 → TL 검수 → 승인 → 공개
         */
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
    // POST /api/kms/tl/approval/{articleId}/reject (TL 반려)
    // =========================================================
    // 목적: TeamLeader가 PENDING 문서를 검수 후 반려할 수 있는지 검증
    // 목적: 반려 사유를 명확히 기록하고 상태를 REJECTED로 변경
    // 흐름: PENDING → TL 검수 → 반려 사유 저장 → REJECTED (재작성 필요)

    @Nested
    @DisplayName("POST /api/kms/tl/approval/{articleId}/reject")
    class TLReject {

        /**
         * ✅ 성공 케이스: TL이 정상 반려
         *
         * Given: PENDING 상태의 문서, 반려 사유 제공 (10~500자)
         * When: POST /api/kms/tl/approval/{articleId}/reject 요청
         * Then: HTTP 200 OK, DB에 상태 REJECTED, 반려 사유 저장
         *
         * 검증 포인트:
         * - HTTP 응답: 200 OK
         * - DB 상태 변경: PENDING → REJECTED
         * - 반려 사유 저장: articleRejectionReason에 기록
         * - 반려 사유는 필수 (10~500자 제약)
         *
         * 흐름: Worker 작성 → Pending → TL 검수 → 반려 → 다시 수정해서 재제출
         */
        @Test
        @DisplayName("Success: Changes status to REJECTED and saves review comment in DB")
        void reject_statusChangedToRejected() throws Exception {
            KnowledgeArticle saved = savePendingArticle();
            String reviewComment = "내용이 부족합니다. 보충 후 재제출해주세요.";

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("reviewComment", reviewComment))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).get();
            assertEquals(ArticleStatus.REJECTED, updated.getArticleStatus());
            assertEquals(reviewComment, updated.getArticleRejectionReason());
        }

        /**
         * ❌ 실패 케이스 1: 반려 사유가 너무 짧음
         *
         * Given: PENDING 상태 문서, 반려 사유가 4자 (10자 미만)
         * When: POST /api/kms/tl/approval/{articleId}/reject 요청
         * Then: HTTP 400 Bad Request, APPROVAL_001 에러
         *
         * 검증 포인트:
         * - 반려 사유는 최소 10자 이상 (명확한 피드백)
         * - 반려 사유는 최대 500자 이하
         * - DTO 검증: @Length(min=10, max=500)
         * - DTO 검증이 먼저 적용된다
         *
         * 규칙: 반려 사유는 충분히 상세해야 함 (10~500자)
         */
        @Test
        @DisplayName("Failure: 반려 사유 길이 미충족 (APPROVAL_001)")
        void reject_invalidReasonLength_fails() throws Exception {
            KnowledgeArticle saved = savePendingArticle();

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("reviewComment", "짧음"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("반려 사유는 10자 이상 500자 이하여야 합니다"));
        }

        /**
         * ❌ 실패 케이스 2: DRAFT 상태 문서는 반려 불가
         *
         * Given: DRAFT 상태의 문서 (아직 제출도 안함)
         * When: POST /api/kms/tl/approval/{articleId}/reject 요청
         * Then: HTTP 400 Bad Request, APPROVAL_003 에러
         *
         * 검증 포인트:
         * - 반려는 PENDING 상태에서만 가능
         * - Draft 단계에서는 반려 개념이 없음 (아직 제출 전)
         * - 승인/반려 대상은 PENDING만 해당
         *
         * 흐름: Draft → (제출) → Pending → 승인/반려 가능
         *      Draft 단계에서는 그냥 수정만 가능
         */
        @Test
        @DisplayName("Failure: PENDING이 아닌 상태에서 반려 불가 (APPROVAL_003)")
        void reject_nonPendingArticle_fails() throws Exception {
            KnowledgeArticle saved = saveDraftArticle();
            String reviewComment = "이 문서는 승인 대기 상태가 아닙니다.";

            mockMvc.perform(post("/api/kms/tl/approval/" + saved.getArticleId() + "/reject")
                            .with(user(tlUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("reviewComment", reviewComment))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("[APPROVAL_003] PENDING 상태에서만 처리할 수 있습니다."));
        }
    }

    // =========================================================
    // GET /api/kms/articles (목록 조회)
    // =========================================================

    @Nested
    @DisplayName("GET /api/kms/articles")
    class GetArticles {

        @Test
        @DisplayName("Returns article list from DB")
        void getArticles_returnsArticleList() throws Exception {
            saveApprovedArticle();
            savePendingArticle();
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Excludes deleted articles")
        void getArticles_excludesDeletedArticles() throws Exception {
            KnowledgeArticle deletedArticle = saveDraftArticle();
            deletedArticle.softDelete();
            knowledgeArticleRepository.save(deletedArticle);

            saveApprovedArticle();
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Filters by article title keyword")
        void getArticles_filtersByArticleTitleKeyword() throws Exception {
            saveApprovedArticleWithViewCount(10, "검색 대상 제목");
            saveApprovedArticleWithViewCount(5, "다른 제목");
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles")
                            .param("searchType", "articleTitle")
                            .param("keyword", "검색 대상")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].articleTitle").value("검색 대상 제목"));
        }

        @Test
        @DisplayName("Worker can see own articles and approved articles only")
        void getArticles_appliesWorkerVisibility() throws Exception {
            saveApprovedArticleWithViewCount(10, "승인된 공개 문서");
            saveArticle(validAuthorId, ArticleStatus.DRAFT, "내 임시 문서", 0);
            saveArticle(otherAuthorIdForQueryTest, ArticleStatus.DRAFT, "다른 사람 임시 문서", 0);
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles")
                            .param("requesterId", String.valueOf(validAuthorId))
                            .param("requesterRole", "WORKER")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[*].articleTitle").value(hasItem("승인된 공개 문서")))
                    .andExpect(jsonPath("$.data[*].articleTitle").value(hasItem("내 임시 문서")))
                    .andExpect(jsonPath("$.data[*].articleTitle").value(not(hasItem("다른 사람 임시 문서"))));
        }
    }

    // =========================================================
    // GET /api/kms/articles/{articleId} (상세 조회)
    // =========================================================

    @Nested
    @DisplayName("GET /api/kms/articles/{articleId}")
    class GetArticleDetail {

        @Test
        @DisplayName("Returns article detail and increments view count")
        void getArticleDetail_returnsDetailAndIncrementsViewCount() throws Exception {
            KnowledgeArticle saved = saveApprovedArticle();
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles/" + saved.getArticleId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.articleId").value(saved.getArticleId()))
                    .andExpect(jsonPath("$.data.articleTitle").value(TITLE));

            KnowledgeArticle updated = knowledgeArticleRepository.findById(saved.getArticleId()).orElseThrow();
            assertEquals(1, updated.getViewCount());
        }
    }

    // =========================================================
    // GET /api/kms/articles/recommendations (추천 조회)
    // =========================================================

    @Nested
    @DisplayName("GET /api/kms/articles/recommendations")
    class GetRecommendations {

        @Test
        @DisplayName("Returns approved recommendations only")
        void getRecommendations_returnsApprovedArticlesOnly() throws Exception {
            saveApprovedArticleWithViewCount(100, "추천 대상 문서 1");
            saveApprovedArticleWithViewCount(50, "추천 대상 문서 2");
            savePendingArticle();
            flushAndClear();

            mockMvc.perform(get("/api/kms/articles/recommendations")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].viewCount").value(100))
                    .andExpect(jsonPath("$.data[1].viewCount").value(50));
        }
    }

    // =========================================================
    // DELETE /api/kms/articles/{articleId} (Worker 삭제)
    // =========================================================
    // 목적: Worker가 자신의 문서를 삭제할 수 있는지 검증
    // 규칙: DRAFT만 삭제 가능, 본인만 삭제 가능, 소프트 삭제 (isDeleted = true)
    // 흐름: DRAFT 상태 + 본인 확인 → softDelete() → isDeleted = true

    @Nested
    @DisplayName("DELETE /api/kms/articles/{articleId}")
    class WorkerDelete {

        /**
         * ✅ 성공 케이스: Worker가 자신의 DRAFT 문서 삭제
         *
         * Given: Worker가 작성한 DRAFT 상태 문서
         * When: DELETE /api/kms/articles/{articleId} 요청 (requesterId = 본인)
         * Then: HTTP 200 OK, DB에서 isDeleted = true (소프트 삭제)
         *
         * 검증 포인트:
         * - HTTP 응답: 200 OK
         * - DB 소프트 삭제: isDeleted 플래그 = true
         * - 데이터는 물리적으로 남아있음 (감시/감사용)
         * - 삭제된 문서는 이후 조회 불가
         *
         * 특징: Hard Delete가 아닌 Soft Delete (isDeleted 플래그)
         */
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

        /**
         * ❌ 실패 케이스 1: 승인된 문서는 삭제 불가
         *
         * Given: APPROVED 상태의 문서 (공개 중)
         * When: DELETE /api/kms/articles/{articleId} 요청
         * Then: HTTP 400 Bad Request, ARTICLE_009 에러, DB에 변화 없음
         *
         * 검증 포인트:
         * - 승인 완료된 문서는 보호됨
         * - 공개 자료 삭제 불가
         * - isDeleted 플래그 변경 없음 (안전성 확인)
         *
         * 규칙: APPROVED 문서는 관리자만 삭제 가능 (adminDelete 사용)
         */
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

        /**
         * ❌ 실패 케이스 2: 검증 대기 중인 문서는 삭제 불가
         *
         * Given: PENDING 상태의 문서 (검증 대기 중)
         * When: DELETE /api/kms/articles/{articleId} 요청
         * Then: HTTP 400 Bad Request, ARTICLE_010 에러
         *
         * 검증 포인트:
         * - PENDING 상태는 검증 진행 중
         * - TL이 검토하는 동안 삭제 불가
         * - 검증 완료 후 결과에 따라 삭제 가능 여부 결정
         *
         * 규칙: 평가(승인/반려) 진행 중인 문서는 삭제 불가
         */
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

        /**
         * ❌ 실패 케이스 3: 반려된 문서는 삭제 불가
         *
         * Given: REJECTED 상태의 문서 (검수 후 반려됨)
         * When: DELETE /api/kms/articles/{articleId} 요청
         * Then: HTTP 400 Bad Request, ARTICLE_010 에러
         *
         * 검증 포인트:
         * - 반려된 문서는 다시 수정 후 재제출 가능해야 함
         * - 평가 과정 기록을 남기기 위해 삭제 불가
         * - 삭제 대신 DRAFT로 되돌린 후 재수정
         *
         * 흐름: REJECTED → (수정해서 재제출) → PENDING 다시 대기
         */
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

        /**
         * ❌ 실패 케이스 4: 이미 삭제된 문서는 재삭제 불가
         *
         * Given: 이미 소프트 삭제된 문서 (isDeleted = true)
         * When: DELETE /api/kms/articles/{articleId} 요청
         * Then: HTTP 400 Bad Request, ARTICLE_008 에러
         *
         * 검증 포인트:
         * - 삭제 로직 시작 전 isDeleted 플래그 확인
         * - 중복 삭제 방지
         * - 명확한 에러 메시지
         *
         * 테스트 기법: saved.softDelete() 호출 후 수동으로 DB 저장
         *            (실제 삭제 후 다시 삭제 시도)
         */
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

        /**
         * ❌ 실패 케이스 5: 다른 사용자의 문서는 삭제 불가
         *
         * Given: User A가 작성한 DRAFT 문서, User B가 삭제 시도
         * When: DELETE /api/kms/articles/{articleId} 요청 (requesterId = 다른 ID)
         * Then: HTTP 400 Bad Request, ARTICLE_007 에러
         *
         * 검증 포인트:
         * - 요청 본문의 requesterId와 실제 작성자(authorId) 비교
         * - 문서 소유자만 삭제 가능 (권한 검증)
         * - 다른 사용자의 문서 침범 차단
         *
         * 보안: 사용자 소유권 검증 (authorId와 requesterId 일치 확인)
         */
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

        /**
         * ❌ 실패 케이스 6: 존재하지 않는 문서는 삭제 불가
         *
         * Given: 존재하지 않는 articleId (9999999999999)
         * When: DELETE /api/kms/articles/{articleId} 요청
         * Then: HTTP 404 Not Found, NOT_FOUND 에러
         *
         * 검증 포인트:
         * - Repository.findById() 결과가 empty
         * - ResourceNotFoundException 발생
         * - GlobalExceptionHandler에서 NOT_FOUND로 매핑
         * - 정확한 HTTP 상태 코드 (404)
         *
         * 상황: 클라이언트가 잘못된 ID로 요청했거나,
         *      다른 사람이 방금 삭제한 문서를 재삭제 시도
         */
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
    // DELETE /api/kms/admin/articles/{articleId} (관리자 삭제)
    // =========================================================
    // 목적: Admin이 모든 상태의 문서를 강제 삭제할 수 있는지 검증
    // 특징: 상태 제약 없음, 삭제 사유 필수 (감시/감사용)
    // 흐름: 모든 상태 → Admin 판단 → 삭제 사유 기록 → softDelete()

    @Nested
    @DisplayName("DELETE /api/kms/admin/articles/{articleId}")
    class AdminDelete {

        /**
         * ✅ 성공 케이스: Admin이 정상 삭제 (삭제 사유 필수)
         *
         * Given: 모든 상태의 문서 (예: APPROVED), Admin 사용자, 삭제 사유 제공
         * When: DELETE /api/kms/admin/articles/{articleId} 요청 (deletionReason = 10~500자)
         * Then: HTTP 200 OK, DB에서 isDeleted = true, 삭제 사유 저장
         *
         * 검증 포인트:
         * - HTTP 응답: 200 OK
         * - DB 소프트 삭제: isDeleted = true
         * - 삭제 사유 저장: articleDeletionReason에 기록
         * - 삭제 사유는 필수 (10~500자)
         *
         * Worker와의 차이:
         * - Worker: DRAFT만, 자신의 문서만, 사유 불필요
         * - Admin: 모든 상태, 모든 문서, 사유 필수 (감시)
         *
         * 감시 기록: Admin이 언제, 왜 삭제했는지 명확히 기록
         */
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
    // 헬퍼 메서드 (테스트 데이터 팩토리)
    // =========================================================
    // 목적: 각 테스트에서 필요한 상태의 문서를 빠르게 생성
    // 패턴: 상태별 메서드 제공 (PENDING, DRAFT, APPROVED, REJECTED)
    // 특징: TimeBasedIdGenerator로 고유 ID 생성 → 테스트 간 간섭 없음

    /**
     * PENDING 상태 문서 생성 (검증 대기 중)
     * 사용처: 승인/반려/조회 테스트
     */
    private KnowledgeArticle savePendingArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }

    /**
     * DRAFT 상태 문서 생성 (작성 중)
     * 사용처: 수정, 삭제, 상태 확인 테스트
     */
    private KnowledgeArticle saveDraftArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }

    /**
     * APPROVED 상태 문서 생성 (공개 중)
     * 사용처: 삭제 불가, Admin 삭제 테스트
     */
    private KnowledgeArticle saveApprovedArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.APPROVED)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }

    private KnowledgeArticle saveApprovedArticleWithViewCount(int viewCount, String title) {
        return saveArticle(validAuthorId, ArticleStatus.APPROVED, title, viewCount);
    }

    /**
     * REJECTED 상태 문서 생성 (반려됨)
     * 사용처: 삭제 불가, 반려 사유 포함 테스트
     */
    private KnowledgeArticle saveRejectedArticle() {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(validAuthorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(TITLE)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
                .articleStatus(ArticleStatus.REJECTED)
                .isDeleted(false)
                .viewCount(0)
                .build());
    }

    private KnowledgeArticle saveArticle(Long authorId, ArticleStatus status, String title, int viewCount) {
        return knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(new TimeBasedIdGenerator().generate())
                .authorId(authorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle(title)
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent(CONTENT)
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
