# KMS 코드 스타일 가이드

> 참조: C:\SWCAMP22\classMaterials\10_msa\00_BaekDongHyeon\msa-root

---

## Entity

```java
@Entity
@Table(name = "knowledge_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    @Enumerated(EnumType.STRING)
    private ArticleStatus articleStatus;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    // Setter 금지 — 비즈니스 메서드로만 상태 변경
    public void submit() { ... }
    public void approve(Long approvedBy, String opinion) { ... }
    public void reject(String reason) { ... }
    public void softDelete() { ... }
}
```

---

## Enum

```java
public enum ArticleStatus {
    DRAFT, PENDING, APPROVED, REJECTED
}

public enum ArticleCategory {
    장애조치, 공정개선, 설비운영, 안전기타
}
```

---

## Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository repository;

    public void register(ArticleCreateRequest request) { ... }
}
```

---

## Repository

```java
public interface KnowledgeArticleRepository
        extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findByArticleIdAndIsDeletedFalse(Long id);
}
```

---

## Controller

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms")
public class KnowledgeArticleController {

    private final KnowledgeArticleService service;

    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody ArticleCreateRequest request) {
        service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.success(null));
    }
}
```

---

## Request DTO

```java
@Getter
@NoArgsConstructor
public class ArticleCreateRequest {
    private String articleTitle;
    private ArticleCategory articleCategory;
    private String articleContent;
}
```

## Response DTO

```java
@Getter
@Builder
public class ArticleDetailResponse {
    private Long articleId;
    private String articleTitle;
    private ArticleStatus articleStatus;
}
```

---

## Test — Entity (순수 Java)

```java
class KnowledgeArticleTest {

    private KnowledgeArticle article;

    @BeforeEach
    void setUp() {
        article = KnowledgeArticle.builder()
                .authorId(1L)
                .articleStatus(ArticleStatus.DRAFT)
                .isDeleted(false)
                .build();
    }

    @Nested
    @DisplayName("한국어 기능명")
    class 기능명Test {

        @Test
        @DisplayName("성공 케이스 설명")
        void 메서드명_Success() {
            // given

            // when
            article.submit();

            // then
            assertEquals(ArticleStatus.PENDING, article.getArticleStatus());
        }

        @Test
        @DisplayName("실패 케이스 설명")
        void 메서드명_ThrowsException() {
            // given & when & then
            assertThrows(IllegalStateException.class, () -> article.submit());
        }
    }
}
```

---

## Test — Service (Mockito BDD)

```java
@ExtendWith(MockitoExtension.class)
class KnowledgeArticleServiceTest {

    @InjectMocks
    private KnowledgeArticleService service;

    @Mock
    private KnowledgeArticleRepository repository;

    private KnowledgeArticle article;

    @BeforeEach
    void setUp() {
        article = KnowledgeArticle.builder()
                .authorId(1L)
                .articleStatus(ArticleStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    @Nested
    @DisplayName("한국어 기능명")
    class 기능명Test {

        @Test
        @DisplayName("성공 케이스")
        void 메서드명_Success() {
            // given
            given(repository.findById(any())).willReturn(Optional.of(article));

            // when
            service.approve(1L, 99L, "의견");

            // then
            assertEquals(ArticleStatus.APPROVED, article.getArticleStatus());
        }
    }
}
```

---

## 핵심 규칙 요약

| 항목 | 규칙 |
|------|------|
| Entity 생성자 | `@NoArgsConstructor(access = PROTECTED)` + `@Builder` + `@AllArgsConstructor` |
| 필드 접근 | `@Getter` only, Setter 금지 |
| 상태 변경 | 비즈니스 메서드 (submit / approve / reject / softDelete) |
| DI | `@RequiredArgsConstructor` + `final` 필드 |
| Enum 매핑 | `@Enumerated(EnumType.STRING)` |
| 응답 | `ApiResponse.success(data)` / `ApiResponse.failure(code, msg)` |
| 테스트 BDD | `given(...).willReturn(...)` |
| 테스트 구조 | `@Nested` + `@DisplayName` 한국어 |
| 주석 | `// given` / `// when` / `// then` |
