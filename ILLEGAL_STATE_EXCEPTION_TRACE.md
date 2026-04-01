#  IllegalStateException 전체 추적 가이드

> `throw new IllegalStateException(...)` 부터 클라이언트 응답까지

---

##  **softDelete()의 IllegalStateException 사용**

### **코드 위치**

```java
// KnowledgeArticle.java (Entity)
public void softDelete() {
    //  1번 예외: 이미 삭제된 문서
    if (Boolean.TRUE.equals(this.isDeleted)) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_008.getMessage());
        //    ↑ 예외 타입      ↑ 예외 메시지
    }
    
    //  2번 예외: PENDING 상태
    if (this.articleStatus == ArticleStatus.PENDING) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
    }
    
    //  3번 예외: REJECTED 상태
    if (this.articleStatus == ArticleStatus.REJECTED) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
    }
    
    //  4번 예외: APPROVED 상태
    if (this.articleStatus == ArticleStatus.APPROVED) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_009.getMessage());
    }
    
    //  모든 조건 통과하면 실행
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
}
```

---

##  **IllegalStateException 전체 흐름**

### **STEP : HTTP 요청 도착**

```http
DELETE /api/kms/articles/1775025046304537
Content-Type: application/json

{
  "requesterId": 1774937276588944
}
```

---

### **STEP : WorkerArticleController 도착**

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/articles")
public class WorkerArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    @DeleteMapping("/{articleId}")  // ← 여기서 요청 받음
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long articleId,  // 1775025046304537
            @Valid @RequestBody ArticleDeleteRequest request  // requesterId: 1774937276588944
    ) {
        //  요청 매핑 성공
        
        //  서비스 호출
        knowledgeArticleService.delete(articleId, request.getRequesterId());
        
        // ← 정상이면 여기 도달 (응답 반환)
        return ResponseEntity.ok(ApiResponse.success(null));
        
        //  예외 발생하면 여기 도달 X (GlobalExceptionHandler로 이동)
    }
}
```

---

### **STEP : KnowledgeArticleService 도착**

```java
@Service
@RequiredArgsConstructor
@Transactional  // ← 트랜잭션 시작
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    public void delete(Long articleId, Long requesterId) {
        //  문서 조회
        KnowledgeArticle article = findArticleById(articleId);
        //                                              ↓
        //                      DB에서 조회 (없으면 ResourceNotFoundException 발생)
        //                      article = { id, status: APPROVED, isDeleted: false }
        
        //  권한 검증
        if (!article.getAuthorId().equals(requesterId)) {
            //   ↑ authorId ≠ requesterId 이면 예외 발생
            //  ARTICLE_007: 본인 문서만 삭제 가능
            throw new IllegalStateException(ArticleErrorCode.ARTICLE_007.getMessage());
            // → GlobalExceptionHandler로 이동
        }
        
        //  소프트 딜리트 호출 ← 예외 발생 가능한 구간
        article.softDelete();  // ← KnowledgeArticle.softDelete() 호출
        //
        //  여기서 4가지 예외 중 하나 발생 가능:
        // - ARTICLE_008: 이미 삭제된 문서
        // - ARTICLE_010: PENDING 상태
        // - ARTICLE_010: REJECTED 상태
        // - ARTICLE_009: APPROVED 상태
        //
        //  예외 발생 → GlobalExceptionHandler로 이동
        //  예외 없음 → article 상태 변경 (isDeleted = true)
    }

    private KnowledgeArticle findArticleById(Long articleId) {
        return knowledgeArticleRepository.findById(articleId)
                .orElseThrow(() -> 
                    new ResourceNotFoundException(
                        ArticleErrorCode.ARTICLE_NOT_FOUND.getMessage()
                    )
                );
    }
}
```

---

### **STEP : KnowledgeArticle.softDelete() 실행**

```java
// KnowledgeArticle.java

public void softDelete() {
    System.out.println("😢 예외 확인 구간");
    System.out.println("isDeleted = " + this.isDeleted);
    System.out.println("status = " + this.articleStatus);
    
    //  CASE 1: 이미 삭제된 문서
    if (Boolean.TRUE.equals(this.isDeleted)) {
        // isDeleted = true 이면 여기 실행
        String errorMessage = ArticleErrorCode.ARTICLE_008.getMessage();
        // errorMessage = "[ARTICLE_008] 이미 삭제된 문서입니다."
        
        throw new IllegalStateException(errorMessage);
        // ⬆️ 새로운 IllegalStateException 객체 생성
        //    - 예외 타입: IllegalStateException.class
        //    - 예외 메시지: "[ARTICLE_008] 이미 삭제된 문서입니다."
        //    - 예외 원인: 비즈니스 로직 위반
        //
        // → 즉시 이 메서드 종료
        // → 스택 unwind (call stack에서 빠져나옴)
        // → GlobalExceptionHandler로 이동
    }
    
    //  CASE 2: PENDING 상태
    if (this.articleStatus == ArticleStatus.PENDING) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        // → GlobalExceptionHandler로 이동
    }
    
    //  CASE 3: REJECTED 상태
    if (this.articleStatus == ArticleStatus.REJECTED) {
        throw new IllegalStateException(ArticleErrorCode.ARTICLE_010.getMessage());
        // → GlobalExceptionHandler로 이동
    }
    
    //  CASE 4: APPROVED 상태
    if (this.articleStatus == ArticleStatus.APPROVED) {
        // articleStatus = APPROVED 이면 여기 실행
        String errorMessage = ArticleErrorCode.ARTICLE_009.getMessage();
        // errorMessage = "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
        
        throw new IllegalStateException(errorMessage);
        // → GlobalExceptionHandler로 이동
    }
    
    //  모든 조건을 통과했다면 정상 삭제 실행
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    System.out.println(" 소프트 딜리트 완료!");
}
```

---

### **STEP 5️⃣: GlobalExceptionHandler 캐치**

```java
// GlobalExceptionHandler.java (전역 예외 처리 핸들러)

@RestControllerAdvice  // ← 모든 Controller의 예외를 받는다
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)  // ← IllegalStateException 타입만 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // ← HTTP 400으로 응답
    public ApiResponse<Void> handleIllegalState(IllegalStateException e) {
        //  여기서 softDelete()에서 throw한 IllegalStateException 객체 받음
        
        // e.getMessage() = "[ARTICLE_008] 이미 삭제된 문서입니다."
        //             또는 "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
        //             또는 "[ARTICLE_010] 평가 진행 중인 문서는 삭제할 수 없습니다."
        
        //  ApiResponse 객체 생성 (JSON으로 변환)
        return ApiResponse.failure("BAD_REQUEST", e.getMessage());
    }
}
```

---

### **STEP 6️⃣: 클라이언트에게 응답**

```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "success": false,
  "data": null,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다.",
  "timestamp": "2026-04-01T15:35:00.1234567"
}
```

---

## 🔀 **CASE별 흐름**

### **CASE A: APPROVED 상태에서 삭제 시도**

```
 HTTP DELETE /api/kms/articles/1775...
       ↓
 WorkerArticleController.delete()
       ↓
 KnowledgeArticleService.delete()
       ↓
 KnowledgeArticle.softDelete()
       ↓
   if (articleStatus == APPROVED) {  ✓ TRUE
       throw new IllegalStateException("[ARTICLE_009]...")
   }
       ↓
 Exception Object 생성 (type: IllegalStateException)
       ↓
 GlobalExceptionHandler.handleIllegalState(IllegalStateException e)
       ↓
 ApiResponse.failure("BAD_REQUEST", "[ARTICLE_009]...")
       ↓
 HTTP 400 + JSON 응답 반환
```

---

### **CASE B: 이미 삭제된 문서 다시 삭제 시도**

```
 HTTP DELETE /api/kms/articles/1775...
       ↓
 WorkerArticleController.delete()
       ↓
 KnowledgeArticleService.delete()
       ↓
 KnowledgeArticle.softDelete()
       ↓
   if (isDeleted == true) {  ✓ TRUE
       throw new IllegalStateException("[ARTICLE_008]...")
   }
       ↓
 Exception Object 생성 (type: IllegalStateException)
       ↓
 GlobalExceptionHandler.handleIllegalState(IllegalStateException e)
       ↓
 ApiResponse.failure("BAD_REQUEST", "[ARTICLE_008]...")
       ↓
 HTTP 400 + JSON 응답 반환
```

---

### **CASE C: 정상 삭제 (DRAFT 상태)**

```
 HTTP DELETE /api/kms/articles/1775...
       ↓
 WorkerArticleController.delete()
       ↓
 KnowledgeArticleService.delete()
       ↓
 KnowledgeArticle.softDelete()
       ↓
   if (isDeleted == true) { ✗ FALSE
   if (articleStatus == PENDING) { ✗ FALSE
   if (articleStatus == REJECTED) { ✗ FALSE
   if (articleStatus == APPROVED) { ✗ FALSE
   
   //  모든 조건 통과!
   this.isDeleted = true;
   this.deletedAt = LocalDateTime.now();
       ↓
 정상 종료 (예외 X)
       ↓
 KnowledgeArticleService.delete() 종료
       ↓
 WorkerArticleController.delete() 종료
       ↓
 ApiResponse.success(null)
       ↓
 HTTP 200 + JSON 응답 반환
```

---

##  **IllegalStateException vs IllegalArgumentException**

### **IllegalStateException (상태 검증)**
- **사용 시점**: 현재 **상태가 잘못된** 경우
- **예시**: APPROVED 상태인데 삭제하려고 함
- **의미**: "지금 이 작업을 할 수 없는 상태야"
- **사용 위치**: 
  - `KnowledgeArticle.softDelete()`
  - `KnowledgeArticle.approve()`
  - `KnowledgeArticle.reject()`
  - `KnowledgeArticleService.delete()` (권한 검증)

### **IllegalArgumentException (값 검증)**
- **사용 시점**: 입력된 **값이 잘못된** 경우
- **예시**: 제목이 4자(5자 미만)
- **의미**: "입력 값이 규칙을 위반했어"
- **사용 위치**: 
  - `KnowledgeArticleService.validateInput()`
  - `KnowledgeArticle.approve()` (의견 길이)
  - `KnowledgeArticle.reject()` (사유 길이)

---

##  **정리**

| 항목 | 설명 |
|------|------|
| **throw한 곳** | KnowledgeArticle.softDelete() |
| **catch한 곳** | GlobalExceptionHandler.handleIllegalState() |
| **HTTP Status** | 400 Bad Request |
| **errorCode** | "BAD_REQUEST" |
| **message** | "[ARTICLE_008/009/010]..." |
| **반환 형식** | ApiResponse.failure() |

---

##  **실제 사용 예시**

### **정상 경우**
```bash
$ curl -X DELETE http://localhost:8080/api/kms/articles/1775025046304537 \
  -H "Content-Type: application/json" \
  -d '{"requesterId": 1774937276588944}'

# 상태: DRAFT → 삭제 가능
Response: HTTP 200
{
  "success": true,
  "data": null
}
```

### **APPROVED 상태 삭제 시도**
```bash
$ curl -X DELETE http://localhost:8080/api/kms/articles/1775025046304537 \
  -H "Content-Type: application/json" \
  -d '{"requesterId": 1774937276588944}'

# 상태: APPROVED → 삭제 불가
Response: HTTP 400
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "[ARTICLE_009] 승인 완료된 문서는 직접 삭제할 수 없습니다."
}
```

---

**이제 IllegalStateException의 전체 흐름이 명확합니다!** 
