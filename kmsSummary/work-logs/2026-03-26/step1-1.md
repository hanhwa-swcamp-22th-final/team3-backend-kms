# STEP 1-1 🔴 Red — KnowledgeArticleTest 작성

---

## 생성 파일

```
src/test/java/com/ohgiraffers/team3backendkms/kms/command/domain/aggregate/KnowledgeArticleTest.java
```

## 테스트 케이스 (총 11개)

| 메서드 | 테스트명 | 검증 |
|--------|----------|------|
| `submit()` | DRAFT → PENDING 성공 | status == PENDING |
| `submit()` | DRAFT 아닌 상태에서 호출 | IllegalStateException |
| `approve()` | PENDING → APPROVED 성공 | status·approvedBy·approvedAt·opinion 검증 |
| `approve()` | PENDING 아닌 상태에서 호출 | IllegalStateException (APPROVAL_003) |
| `approve()` | 의견 501자 | IllegalArgumentException (APPROVAL_002) |
| `reject()` | PENDING → REJECTED 성공 | status·reason 검증 |
| `reject()` | 사유 10자 미만 | IllegalArgumentException (APPROVAL_001) |
| `reject()` | 사유 501자 | IllegalArgumentException (APPROVAL_001) |
| `reject()` | PENDING 아닌 상태에서 호출 | IllegalStateException (APPROVAL_003) |
| `softDelete()` | 소프트 딜리트 성공 | isDeleted=true, deletedAt not null |
| `softDelete()` | APPROVED 상태에서 호출 | IllegalStateException (ARTICLE_009) |
| `softDelete()` | 이미 삭제된 문서 재삭제 | IllegalStateException (ARTICLE_008) |

## 현재 상태

- KnowledgeArticle, ArticleStatus, ArticleCategory 클래스 없음 → 컴파일 에러 (정상 🔴)

## 다음 작업

- STEP 1-2 🟢 Green — ArticleStatus, ArticleCategory, KnowledgeArticle 구현
