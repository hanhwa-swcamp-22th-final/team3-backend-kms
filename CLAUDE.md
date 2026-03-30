# KMS 프로젝트 — Claude 참조 파일

> 이 파일은 매 세션 시작 시 반드시 읽고 진행할 것.

---

## 프로젝트 기본 정보

- **프로젝트**: team3-backend-kms
- **도메인**: KMS 지식허브 (KnowledgeHub)
- **Spring Boot**: 3.5.12 / Java 21
- **아키텍처**: CQRS (JPA Command + MyBatis Query)
- **DB**: MariaDB (실제 DB 사용, H2 미사용)
- **현재 브랜치**: feat/kms-tdd-test

---

## 절대 규칙

1. **KMS 프로젝트만 작업한다.**
   - `team3-backend-admin`은 구조 참고용으로만 읽는다.
   - admin 프로젝트 코드는 절대 수정하지 않는다.

2. **한 스텝씩 진행한다.**
   - 사용자가 확인 후 다음 스텝을 요청할 때까지 기다린다.
   - 여러 스텝을 한 번에 진행하지 않는다.

3. **작업 전 반드시 파일을 읽는다.**
   - 코드를 수정하기 전 항상 현재 파일 상태를 먼저 확인한다.

---

## 프로젝트 구조

```
src/main/java/com/ohgiraffers/team3backendkms/
├── auth/command/                        JWT 인증 (로그인, 토큰)
├── config/
│   ├── JpaAuditingConfig.java           @EnableJpaAuditing(auditorAwareRef = "auditorProvider")
│   ├── SecurityConfig.java
│   └── security/
│       ├── AuditorAwareImpl.java        SecurityContext → employeeId 추출
│       └── CustomUserDetails.java       Spring User 상속, employeeId 추가
├── common/
│   ├── dto/ApiResponse.java             공통 응답 포맷 (여기만 사용, 중복 주의)
│   ├── exception/GlobalExceptionHandler.java
│   └── idgenerator/TimeBasedIdGenerator.java
└── kms/
    ├── command/                         JPA (등록, 수정, 삭제, 승인, 반려)
    └── query/                           MyBatis (조회)
```

---

## JPA Auditing 규칙 (필수)

> 모든 엔티티에 적용. 서비스에서 직접 세팅 금지.

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SomeEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
```

**흐름**:
```
JWT → JwtAuthenticationFilter
    → CustomUserDetailsService.findByEmployeeCode()
    → CustomUserDetails (employeeId 포함)
    → SecurityContextHolder
    → save() 시 AuditorAwareImpl.getCurrentAuditor()
    → createdBy / updatedBy 자동 세팅
```

---

## 현재 구현 완료 API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/kms/articles` | 지식 문서 등록 |
| POST | `/api/kms/articles/drafts` | 임시저장 |
| POST | `/api/kms/approval/{articleId}/approve` | 승인 |
| POST | `/api/kms/approval/{articleId}/reject` | 반려 |
| DELETE | `/api/kms/articles/{articleId}` | 삭제 |
| GET | `/api/kms/articles` | 목록 조회 |
| GET | `/api/kms/articles/{articleId}` | 상세 조회 |

---

## 테스트 현황

```
71 PASS / 0 SKIP / 0 FAIL (2026-03-29 기준)
```

| 파일 | 종류 |
|------|------|
| `KnowledgeArticleTest` | 도메인 단위 |
| `KnowledgeArticleServiceTest` | 서비스 단위 (Mockito) |
| `KnowledgeArticleServiceIntegrationTest` | 서비스 통합 (실제 DB) |
| `KnowledgeArticleRepositoryTest` | 레포지토리 |
| `KnowledgeArticleCommandControllerTest` | 커맨드 컨트롤러 |
| `KnowledgeArticleQueryControllerTest` | 쿼리 컨트롤러 |
| `KnowledgeArticleQueryMapperTest` | MyBatis 매퍼 (실제 DB) |
| `KnowledgeArticleQueryServiceTest` | 쿼리 서비스 |

---

## 코드 스타일

자세한 내용은 아래 파일 참조:
- `kmsSummary/file/code-style/style-guide.md` — KMS 코드 스타일 + JPA Auditing
- `kmsSummary/file/code-style/code-convention.md` — 팀 전체 컨벤션

**핵심 규칙 요약**:
- Entity: `@NoArgsConstructor(PROTECTED)` + `@Builder` + `@AllArgsConstructor`
- Setter 금지 — 비즈니스 메서드로만 상태 변경
- `@MockBean` 대신 `@MockitoBean` 사용 (Spring Boot 3.4+)
- `common/dto/ApiResponse.java` 만 사용 (`common/ApiResponse.java` 삭제됨)

---

## 트러블슈팅 기록

`kmsSummary/file/troubleshooting/spring-boot-version-test-issues.md`

주요 이슈:
- Spring Boot 4.0.4 → 3.5.12 다운그레이드
- `@MockBean` → `@MockitoBean`
- `@WebMvcTest` + Security 충돌 → `excludeAutoConfiguration`
- 통합 테스트 FK 제약 → `SET FOREIGN_KEY_CHECKS=0` + `INSERT IGNORE`
- JPA → MyBatis 가시성 → `entityManager.flush()`

---

## 워크로그

`kmsSummary/work-logs/` 날짜별 폴더로 관리
- `work-summary.md` — 당일 작업 요약
- `trouble.md` — 트러블슈팅 기록
