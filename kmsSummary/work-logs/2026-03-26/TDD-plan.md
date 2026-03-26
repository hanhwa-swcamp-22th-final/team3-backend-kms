# TDD 진행 계획 - 2026-03-26

---

## 참고 구조
- `AuthServiceTest.java` 구조 그대로 따름
- `given(...).willReturn(...)` BDD 스타일
- `@Nested` + `@DisplayName` 한국어
- `// given / // when / // then` 주석

---

## 전체 스텝 현황

| 스텝 | 내용 | 상태 |
|------|------|------|
| STEP 1-1 | 🔴 Red — KnowledgeArticleTest 작성 | ⬜ |
| STEP 1-2 | 🟢 Green — KnowledgeArticle 엔티티 구현 | ⬜ |
| STEP 1-3 | 🔵 Refactor — Entity 정리 | ⬜ |
| STEP 2-1 | 🔴 Red — KnowledgeArticleServiceTest 작성 | ⬜ |
| STEP 2-2 | 🟢 Green — KnowledgeArticleService 구현 | ⬜ |
| STEP 2-3 | 🔵 Refactor — Service 정리 | ⬜ |
| STEP 3-1 | 🗄️ DB — Repository 테스트 (@DataJpaTest) | ⬜ |
| STEP 3-2 | 🗄️ DB — 전체 통합 테스트 (@SpringBootTest) | ⬜ |

---

## STEP 1 — Entity 단위 테스트 (순수 Java)

### STEP 1-1 🔴 Red — KnowledgeArticleTest 작성
> 아직 엔티티 없음 → 컴파일 에러 상태가 정상

- [ ] `ArticleStatus` Enum 참조 (DRAFT / PENDING / APPROVED / REJECTED)
- [ ] `ArticleCategory` Enum 참조
- [ ] `KnowledgeArticle.builder()` 로 테스트 데이터 세팅
- [ ] `@BeforeEach setUp()` 공통 article 객체 준비
- [ ] submit() 테스트
  - [ ] DRAFT → PENDING 성공
  - [ ] DRAFT 아닌 상태에서 submit → IllegalStateException
- [ ] approve() 테스트
  - [ ] PENDING → APPROVED, approvedBy·approvedAt 저장 성공
  - [ ] PENDING 아닌 상태 → IllegalStateException (APPROVAL_003)
  - [ ] 의견 500자 초과 → IllegalArgumentException (APPROVAL_002)
- [ ] reject() 테스트
  - [ ] PENDING → REJECTED, reason 저장 성공
  - [ ] reason 10자 미만 → IllegalArgumentException (APPROVAL_001)
  - [ ] reason 500자 초과 → IllegalArgumentException (APPROVAL_001)
  - [ ] PENDING 아닌 상태 → IllegalStateException (APPROVAL_003)
- [ ] softDelete() 테스트
  - [ ] is_deleted=true, deletedAt 저장 성공
  - [ ] APPROVED 상태 직접 삭제 → IllegalStateException (ARTICLE_009)
  - [ ] 이미 삭제된 문서 재삭제 → IllegalStateException (ARTICLE_008)

**생성 파일**
```
src/test/.../kms/command/domain/aggregate/KnowledgeArticleTest.java
```

---

### STEP 1-2 🟢 Green — KnowledgeArticle 엔티티 구현
> 테스트를 통과시키기 위한 최소 구현

- [ ] `ArticleStatus.java` — ENUM 생성
- [ ] `ArticleCategory.java` — ENUM 생성
- [ ] `KnowledgeArticle.java` — 엔티티 + 비즈니스 로직 메서드
  - [ ] `submit()`
  - [ ] `approve(approvedBy, opinion)`
  - [ ] `reject(reason)`
  - [ ] `softDelete()`
- [ ] 테스트 전체 통과 확인 ✅

**생성 파일**
```
src/main/.../kms/command/domain/aggregate/ArticleStatus.java
src/main/.../kms/command/domain/aggregate/ArticleCategory.java
src/main/.../kms/command/domain/aggregate/KnowledgeArticle.java
```

---

### STEP 1-3 🔵 Refactor — Entity 정리
> 기능 훼손 없이 코드 품질 개선

- [ ] 예외 메시지 상수화
- [ ] 유효성 검증 로직 중복 제거
- [ ] 테스트 재통과 확인 ✅

---

## STEP 2 — Service 단위 테스트 (Mockito)

### STEP 2-1 🔴 Red — KnowledgeArticleServiceTest 작성
> AuthServiceTest 구조 동일하게 적용

- [ ] `@ExtendWith(MockitoExtension.class)`
- [ ] `@InjectMocks KnowledgeArticleService`
- [ ] `@Mock KnowledgeArticleRepository`
- [ ] `@BeforeEach setUp()` — 공통 article Mock 데이터 준비
- [ ] register() 테스트
  - [ ] 저장 후 status=PENDING 확인
  - [ ] 제목 5자 미만 → 예외 (ARTICLE_001)
  - [ ] 본문 50자 미만 → 예외 (ARTICLE_002)
  - [ ] 본문 10,000자 초과 → 예외 (ARTICLE_003)
- [ ] draft() 테스트
  - [ ] 저장 후 status=DRAFT 확인
- [ ] getDetail() 테스트
  - [ ] viewCount +1 호출 확인
  - [ ] is_deleted=true → 예외 (ARTICLE_008)
- [ ] approve() 테스트
  - [ ] status=APPROVED 전환 확인
  - [ ] PENDING 아닌 상태 → 예외 (APPROVAL_003)
- [ ] reject() 테스트
  - [ ] status=REJECTED + reason 저장 확인
  - [ ] reason 없음 → 예외 (APPROVAL_001)
- [ ] delete() 테스트
  - [ ] is_deleted=true 확인
  - [ ] 타인 문서 삭제 시도 → 예외 (ARTICLE_007)
  - [ ] APPROVED 상태 삭제 시도 → 예외 (ARTICLE_009)

**생성 파일**
```
src/test/.../kms/command/application/service/KnowledgeArticleServiceTest.java
```

---

### STEP 2-2 🟢 Green — KnowledgeArticleService 구현
> 테스트를 통과시키기 위한 최소 구현

- [ ] `KnowledgeArticleRepository.java` (interface)
- [ ] `KnowledgeArticleService.java`
  - [ ] `register()`
  - [ ] `draft()`
  - [ ] `getDetail()`
  - [ ] `approve()`
  - [ ] `reject()`
  - [ ] `delete()`
- [ ] 테스트 전체 통과 확인 ✅

---

### STEP 2-3 🔵 Refactor — Service 정리
- [ ] 공통 예외 처리 정리
- [ ] 테스트 재통과 확인 ✅

---

## STEP 3 — DB 통합 테스트

### STEP 3-1 🗄️ Repository 테스트 (@DataJpaTest)
- [ ] `findByAuthorId` 동작 확인
- [ ] `findByArticleStatus` 동작 확인
- [ ] `is_deleted=false` 필터 동작 확인

### STEP 3-2 🗄️ 전체 통합 테스트 (@SpringBootTest)
- [ ] 테스트 전용 데이터 INSERT
- [ ] Controller → Service → DB 전체 흐름
- [ ] 검증 후 @Transactional 롤백

---

## 작업 기록

| 시각 | 내용 | 상태 |
|------|------|------|
| | TDD 플랜 수립 및 워크로그 작성 | ✅ |
