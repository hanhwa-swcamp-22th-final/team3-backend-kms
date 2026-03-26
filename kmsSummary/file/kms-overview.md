# KMS 전체 개요

> 작성일: 2026-03-26
> 브랜치: feat/kms-tdd-test

---

## 1. 프로젝트 기본 정보

| 항목 | 값 |
|------|----|
| 그룹 | `com.ohgiraffers` |
| 패키지 루트 | `com.ohgiraffers.team3backendkms` |
| Java | 21 |
| Spring Boot | 4.0.4 |
| DB | MySQL (`setodb`) |
| 아키텍처 | CQRS (Command / Query 분리) |
| ORM | JPA (CUD) + MyBatis (Read) |

---

## 2. 의존성

```groovy
spring-boot-starter-web
spring-boot-starter-data-jpa
mybatis-spring-boot-starter:3.0.4
mysql-connector-j
lombok
spring-boot-starter-test
junit-platform-launcher
```

---

## 3. 패키지 구조

```
com.ohgiraffers.team3backendkms/
├── common/
│   └── ApiResponse.java                  ← 공통 응답 래퍼 (완성)
├── config/                               ← Spring 설정 (예정)
├── exception/                            ← 예외 처리 (예정)
├── auth/                                 ← 인증 (예정)
├── jwt/                                  ← JWT (예정)
└── kms/
    ├── command/                          ← 쓰기 (CUD)
    │   ├── application/
    │   │   ├── controller/               ← REST 컨트롤러
    │   │   ├── dto/request/              ← 요청 DTO
    │   │   ├── dto/response/             ← 응답 DTO
    │   │   └── service/
    │   │       └── KnowledgeArticleService.java
    │   ├── domain/
    │   │   ├── aggregate/
    │   │   │   ├── ArticleStatus.java    ← Enum
    │   │   │   ├── ArticleCategory.java  ← Enum
    │   │   │   └── KnowledgeArticle.java ← Entity
    │   │   ├── repository/
    │   │   │   └── KnowledgeArticleRepository.java
    │   │   └── service/
    │   └── infrastructure/
    │       ├── repository/
    │       └── service/
    └── query/                            ← 읽기 (R)
        ├── controller/
        ├── dto/request/
        ├── dto/response/
        ├── mapper/                       ← MyBatis Mapper
        └── service/
```

---

## 4. DB 테이블 (KMS 관련)

### knowledge_article (핵심)

| 컬럼 | 타입 | 제약 |
|------|------|------|
| `article_id` | BIGINT | PK |
| `author_id` | BIGINT | FK → employee |
| `approved_by` | BIGINT | FK → employee (nullable) |
| `equipment_id` | BIGINT | FK |
| `file_group_id` | BIGINT | FK |
| `article_title` | VARCHAR(255) | 5~200자 |
| `article_category` | ENUM | 장애조치 / 공정개선 / 설비운영 / 안전 기타 |
| `article_content` | TEXT | 50~10,000자 |
| `article_status` | ENUM | Draft / Pending / Approved / Rejected |
| `article_approval_opinion` | VARCHAR(500) | 최대 500자 |
| `approved_at` | TIMESTAMP | nullable |
| `article_rejection_reason` | VARCHAR(500) | 10~500자 |
| `article_deletion_reason` | VARCHAR(500) | 10~500자 |
| `deleted_at` | TIMESTAMP | nullable |
| `is_deleted` | BOOLEAN | 소프트 딜리트 |
| `view_count` | INT | 기본값 0 |
| `created_at` / `updated_at` | TIMESTAMP | Audit |

### knowledge_edit_history

| 컬럼 | 타입 |
|------|------|
| `history_id` | BIGINT PK |
| `article_id` | BIGINT FK |
| `editor_id` | BIGINT FK |
| `article_previous_content` | TEXT |
| `article_new_content` | TEXT |
| `edited_at` | TIMESTAMP |

### knowledge_tag / knowledge_article_tag

| 테이블 | 컬럼 |
|--------|------|
| `knowledge_tag` | tag_id, tag_name |
| `knowledge_article_tag` | tag_id, article_id (중간 테이블) |

---

## 5. Entity 비즈니스 로직

### ArticleStatus Enum
```
DRAFT → PENDING → APPROVED
                → REJECTED
```

### ArticleCategory Enum
```
장애조치 / 공정개선 / 설비운영 / 안전 기타
```

### KnowledgeArticle 핵심 메서드

| 메서드 | 규칙 |
|--------|------|
| `submit()` | DRAFT → PENDING. 비 DRAFT면 IllegalStateException |
| `approve(approvedBy, opinion)` | PENDING → APPROVED, approvedBy·approvedAt 저장. 비 PENDING → APPROVAL_003. 의견 500자 초과 → APPROVAL_002 |
| `reject(reason)` | PENDING → REJECTED, reason 저장. 10자 미만·500자 초과 → APPROVAL_001. 비 PENDING → APPROVAL_003 |
| `softDelete()` | is_deleted=true, deletedAt 저장. APPROVED 직접 삭제 → ARTICLE_009. 이미 삭제 → ARTICLE_008 |

---

## 6. API 목록

### 1순위 — 바로 구현 가능

| Method | URL | 기능 | Actor |
|--------|-----|------|-------|
| GET | `/api/kms/articles` | 지식 목록 조회 | TL·DL·Worker·Admin |
| GET | `/api/kms/articles/{articleId}` | 지식 상세 조회 | TL·DL·Worker·Admin |
| POST | `/api/kms/articles` | 지식 등록 (PENDING) | Worker |
| POST | `/api/kms/articles/drafts` | 임시저장 (DRAFT) | Worker |
| POST | `/api/kms/approval/{articleId}/approve` | 승인 처리 | TL·DL |
| POST | `/api/kms/approval/{articleId}/reject` | 반려 처리 | TL·DL |
| GET | `/api/kms/my/articles` | 내 지식 목록 | Worker |
| GET | `/api/kms/my/articles/stats` | 내 지식 통계 | Worker |
| PUT | `/api/kms/articles/{articleId}` | 지식 수정 제출 | Worker |
| PUT | `/api/kms/articles/{articleId}/draft` | 수정 임시저장 | Worker |
| DELETE | `/api/kms/articles/{articleId}` | 내 지식 삭제 | Worker |
| GET | `/api/kms/articles/{articleId}/history` | 수정 이력 조회 | Worker |

### 2순위 — 조인/파생 필요

| Method | URL | 기능 |
|--------|-----|------|
| GET | `/api/kms/articles/contributors` | 월간 기여자 랭킹 |
| GET | `/api/kms/articles/recommendations` | AI 지식 추천 |
| GET | `/api/kms/approval/stats` | 승인 통계 |
| GET | `/api/kms/approval` | 승인 대기 목록 |
| GET | `/api/kms/approval/{articleId}` | 승인 항목 상세 |
| GET | `/api/employees/{employeeId}/skill-gap` | 스킬갭 조회 |
| GET | `/api/employees/{employeeId}/skill-gap/summary` | 스킬갭 요약 |
| GET | `/api/employees/{employeeId}/skill-gap/articles` | 관련 KMS 지식 |

### 3순위 — 스키마 확장 필요

| 기능 | 신규 테이블 |
|------|------------|
| 댓글 CRUD | `knowledge_comment` |
| 구독 등록/해제 | `knowledge_subscription` |
| 멘토링 신청/수락/거절 | `mentoring_request`, `mentoring` |
| AI 검토 | `knowledge_ai_review` |
| AI 스킬갭 분석 | `skill_gap_analysis` |
| 학습 추천/시작 | `learning_course`, `learning_record` |

---

## 7. 에러 코드

### ARTICLE (지식 문서)

| 코드 | 설명 |
|------|------|
| ARTICLE_001 | 제목 길이 위반 (5~200자) |
| ARTICLE_002 | 본문 최소 길이 미충족 (최소 50자) |
| ARTICLE_003 | 본문 최대 길이 초과 (최대 10,000자) |
| ARTICLE_004 | 유효하지 않은 카테고리 값 |
| ARTICLE_005 | 존재하지 않는 설비 ID |
| ARTICLE_006 | Pending 상태 지식 수정 불가 |
| ARTICLE_007 | 타인 지식 문서 수정·삭제 권한 없음 |
| ARTICLE_008 | 이미 삭제된 지식 문서 접근 불가 |
| ARTICLE_009 | Approved 지식 직접 삭제 불가 |

### APPROVAL (KMS 승인)

| 코드 | 설명 |
|------|------|
| APPROVAL_001 | 반려 사유 길이 위반 (10~500자 필수) |
| APPROVAL_002 | 승인 의견 최대 길이 초과 (최대 500자) |
| APPROVAL_003 | Pending 아닌 문서에 승인·반려·보류 불가 |
| APPROVAL_004 | AI 검토 미완료 상태에서 승인 불가 |

### 기타

| 코드 | 설명 |
|------|------|
| COMMENT_001~002 | 댓글 내용 미입력 / 타인 댓글 수정·삭제 불가 |
| SUBSCRIBE_001~002 | 중복 구독 불가 / 본인 문서 구독 불가 |
| MENTORING_001~004 | 중복 신청 / 수정 불가 / 본인 신청 불가 / 필수항목 미입력 |
| SKILL_001~002 | 스킬 데이터 없음 / AI 분석 결과 없음 |
| ATTACH_001 | 허용되지 않는 파일 형식 |

---

## 8. TDD 진행 현황

| 스텝 | 내용 | 상태 |
|------|------|------|
| STEP 1-1 | 🔴 Red — KnowledgeArticleTest 작성 | ⬜ |
| STEP 1-2 | 🟢 Green — Entity 구현 (ArticleStatus / ArticleCategory / KnowledgeArticle) | ⬜ |
| STEP 1-3 | 🔵 Refactor — Entity 정리 | ⬜ |
| STEP 2-1 | 🔴 Red — KnowledgeArticleServiceTest 작성 | ⬜ |
| STEP 2-2 | 🟢 Green — KnowledgeArticleService 구현 | ⬜ |
| STEP 2-3 | 🔵 Refactor — Service 정리 | ⬜ |
| STEP 3-1 | 🗄️ @DataJpaTest — Repository 테스트 | ⬜ |
| STEP 3-2 | 🗄️ @SpringBootTest — 전체 통합 테스트 | ⬜ |

---

## 9. 테스트 파일 경로

```
src/test/java/com/ohgiraffers/team3backendkms/
├── config/DatabaseConnectionTest.java                              ← 완성 ✅
├── kms/command/domain/aggregate/KnowledgeArticleTest.java          ← STEP 1-1
└── kms/command/application/service/KnowledgeArticleServiceTest.java ← STEP 2-1

src/main/java/com/ohgiraffers/team3backendkms/
├── common/ApiResponse.java                                         ← 완성 ✅
└── kms/command/domain/aggregate/
    ├── ArticleStatus.java                                          ← STEP 1-2
    ├── ArticleCategory.java                                        ← STEP 1-2
    └── KnowledgeArticle.java                                       ← STEP 1-2
```

---

## 10. 테스트 스타일 규칙

```java
// 클래스 구조
@ExtendWith(MockitoExtension.class)
class KnowledgeArticleServiceTest {

    @Nested
    @DisplayName("한국어로 기능명")
    class 기능명Test {

        @Test
        @DisplayName("성공 케이스 설명")
        void 메서드명_Success() {
            // given
            given(...).willReturn(...);

            // when
            ...

            // then
            assertThat(...).isEqualTo(...);
        }
    }
}
```
