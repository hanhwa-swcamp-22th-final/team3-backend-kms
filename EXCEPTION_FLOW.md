# 📊 softDelete() 예외 처리 전체 흐름

> `IllegalStateException` 발생부터 클라이언트 응답까지

---

## 🔄 **전체 처리 흐름**

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP 요청                                │
│  DELETE /api/kms/articles/{articleId}?requesterId=1         │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│         WorkerArticleController.delete()                    │
│  @DeleteMapping("/{articleId}")                             │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│         KnowledgeArticleService.delete()                    │
│  - authorId 검증                                            │
│  - article 조회                                             │
│  - article.softDelete() 호출 ← 여기서 예외 발생 가능       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│       KnowledgeArticle.softDelete()                         │
│  (도메인 엔티티)                                             │
└────────────────┬────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
    ❌ 조건 위반      ✅ 모두 통과
    (예외 발생)       (정상 삭제)
        │                 │
        ▼                 ▼
throw                    this.isDeleted = true
IllegalStateException    this.deletedAt = now()
        │                 │
        └────────┬────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│      GlobalExceptionHandler (전역 예외 처리)                │
│  @RestControllerAdvice                                      │
│                                                              │
│  @ExceptionHandler(IllegalStateException.class)            │
│  @ResponseStatus(HttpStatus.BAD_REQUEST)  ← HTTP 400      │
│                                                              │
│  return ApiResponse.failure("BAD_REQUEST", message);       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│              클라이언트 응답 (JSON)                          │
│  HTTP 400 Bad Request                                       │
│                                                              │
│  {                                                          │
│    "success": false,                                        │
│    "data": null,                                            │
│    "errorCode": "BAD_REQUEST",                             │
│    "message": "[ARTICLE_XXX] 메시지",                      │
│    "timestamp": "2026-04-01T15:30:00"                      │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 **softDelete()에서 발생 가능한 4가지 예외**

### **1️⃣ ARTICLE_008: 이미 삭제된 문서**

```java
if (Boolean.TRUE.equals(this.isDeleted)) {
    throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
}

// 에러 메시지: "[ARTICLE_008] 이미 삭제된 문서입니다."

// 응답 예시:
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_008] 이미 삭제된 문서입니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

**발생 조건**: `isDeleted == true` 상태에서 다시 삭제 시도

---

### **2️⃣ ARTICLE_010: 평가 진행 중인 문서는 삭제 불가 (PENDING)**

```java
if (this.articleStatus == ArticleStatus.PENDING) {
    throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
}

// 에러 메시지: "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."

// 응답 예시:
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

**발생 조건**: `articleStatus == PENDING` (승인 대기 상태)

---

### **3️⃣ ARTICLE_010: 평가 진행 중인 문서는 삭제 불가 (REJECTED)**

```java
if (this.articleStatus == ArticleStatus.REJECTED) {
    throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
}

// 에러 메시지: "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."

// 응답 예시: (PENDING과 동일)
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

**발생 조건**: `articleStatus == REJECTED` (반려된 상태)

---

### **4️⃣ ARTICLE_009: 승인 완료 문서는 직접 삭제 불가**

```java
if (this.articleStatus == ArticleStatus.APPROVED) {
    throw new IllegalStateException(ArticleErrorCode.ARTICLE_009.getMessage());
}

// 에러 메시지: "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."

// 응답 예시:
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

**발생 조건**: `articleStatus == APPROVED` (승인 완료 상태)

---

## 📝 **소스코드 흐름**

### **1단계: 컨트롤러에서 호출**

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long articleId,
            @RequestParam Long requesterId
    ) {
        knowledgeArticleService.delete(articleId, requesterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

### **2단계: 서비스에서 처리**

```java
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    /* 지식 문서 삭제 (Worker) */
    public void delete(Long articleId, Long requesterId) {
        // 1️⃣ 문서 조회
        KnowledgeArticle article = findArticleById(articleId);

        // 2️⃣ 권한 검증 (본인 문서만 삭제 가능)
        if (!article.getAuthorId().equals(requesterId)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
        }

        // 3️⃣ 소프트 딜리트 호출 ← 여기서 예외 발생 가능
        article.softDelete();  // ❌ IllegalStateException 발생 가능
        // ✅ 예외 없으면 자동 저장 (@Transactional 커밋)
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(ArticleErrorCode.ARTICLE_NOT_FOUND.getMessage()));
    }
}
```

### **3단계: 엔티티에서 비즈니스 로직**

```java
@Entity
@Table(name = "knowledge_article")
@Getter
public class KnowledgeArticle {

    private Long articleId;
    private ArticleStatus articleStatus;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    /* 소프트 딜리트 */
    public void softDelete() {
        // 1️⃣ 이미 삭제됨?
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        }
        
        // 2️⃣ PENDING 상태?
        if (this.articleStatus == ArticleStatus.PENDING) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        }
        
        // 3️⃣ REJECTED 상태?
        if (this.articleStatus == ArticleStatus.REJECTED) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        }
        
        // 4️⃣ APPROVED 상태?
        if (this.articleStatus == ArticleStatus.APPROVED) {
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_009.getMessage());
        }

        // ✅ 모든 조건을 통과하면 소프트 딜리트 실행
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
```

### **4단계: 글로벌 예외 핸들러에서 처리**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // IllegalStateException을 BAD_REQUEST로 변환
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // ← HTTP 400
    public ApiResponse<Void> handleIllegalState(IllegalStateException e) {
        // e.getMessage()는 "[ARTICLE_XXX] 메시지"
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }
}
```

### **5단계: 클라이언트 응답**

```json
{
  "success": false,
  "data": null,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_008] 이미 삭제된 문서입니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

---

## 🎯 **상태별 삭제 가능 여부**

```
DRAFT ────────────────────────── ✅ 삭제 가능
        (아직 제출 전)

PENDING ──────────────────────── ❌ 삭제 불가
        (승인 대기 중 — ARTICLE_010)

APPROVED ─────────────────────── ❌ 삭제 불가
        (승인 완료 — ARTICLE_009)
        (관리자만 삭제 가능)

REJECTED ─────────────────────── ❌ 삭제 불가
        (반려됨 — ARTICLE_010)
```

---

## 📋 **예외 처리 요약표**

| 예외 | HTTP | errorCode | message |
|------|------|-----------|---------|
| **IllegalStateException** (softDelete) | 400 | BAD_REQUEST | [ARTICLE_008/009/010] ... |
| **IllegalStateException** (다른 메서드) | 400 | BAD_REQUEST | [ARTICLE/APPROVAL_XXX] ... |
| **IllegalArgumentException** (검증) | 400 | BAD_REQUEST | [ARTICLE_001~004] ... |
| **ResourceNotFoundException** | 404 | NOT_FOUND | [ARTICLE] 문서를 찾을 수 없습니다. |
| **MethodArgumentNotValidException** | 400 | VALIDATION_ERROR | 필드 검증 오류 |
| **Exception** (예상 못한 오류) | 500 | INTERNAL_ERROR | 서버 오류가 발생했습니다. |

---

## 🧪 **테스트 예시**

### **성공 케이스**
```http
DELETE /api/kms/articles/1775025046304537?requesterId=1774937276588944

// 상태: DRAFT (삭제 가능)
// 결과: ✅ 204 No Content
```

### **실패 케이스 1: APPROVED 상태에서 삭제 시도**
```http
DELETE /api/kms/articles/1775025046304537?requesterId=1774937276588944

// 상태: APPROVED
// 결과: ❌ 400 Bad Request
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

### **실패 케이스 2: 이미 삭제된 문서 다시 삭제 시도**
```http
DELETE /api/kms/articles/1775025046304537?requesterId=1774937276588944

// 상태: 이미 삭제됨 (isDeleted = true)
// 결과: ❌ 400 Bad Request
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_008] 이미 삭제된 문서입니다.",
  "timestamp": "2026-04-01T15:30:00"
}
```

---

## 💡 **정리**

| 질문 | 답변 |
|------|------|
| **어디서 사용되나?** | KnowledgeArticleService.delete() 메서드에서 호출 |
| **어디서 처리되나?** | GlobalExceptionHandler의 @ExceptionHandler(IllegalStateException.class) |
| **에러코드가 뭔가?** | ARTICLE_008, ARTICLE_009, ARTICLE_010 중 하나 + HTTP 400 |
| **최종 응답은?** | ApiResponse.failure("BAD_REQUEST", "[ARTICLE_XXX] 메시지") |

---

**이제 전체 흐름이 명확하죠?** ✅
