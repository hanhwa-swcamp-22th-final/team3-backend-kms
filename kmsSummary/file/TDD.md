# 1. TDD(Test-Driven Development)란?

- **”실제 동작하는 코드를 작성하기 전에, 그 코드가 통과해야 할 테스트 코드를 먼저 작성하는 개발 방법론”**
- 전통적인 개발 방식이 `설계 ➔ 개발 ➔ 테스트` 순서라면,
  TDD는 **`설계 ➔ 테스트 코드 작성 ➔ 개발 ➔ 리팩토링`**의 순서로 진행됨.

---

# 2. 왜 TDD를 해야 하는가? (목적과 장점)

- **안정성 확보 (버그 사전 차단):** 엣지 케이스(예외 상황)를 먼저 고민하고 방어 로직을 짜게 되므로 배포 후 치명적인 버그가 줄어듦.
- **거침없는 리팩토링:** 기존 테스트 코드가 존재하므로, 기능 추가나 코드 수정 시 부작용(Side-effect) 발생 여부를 즉각 확인할 수 있음.
- **살아있는 API 문서:** 테스트 코드 자체가 해당 메서드의 스펙과 예외 상황을 명확히 보여주는 최신 명세서 역할을 함.
- **객체 지향적 설계 유도:** 테스트하기 쉬운 코드를 작성하려면 필연적으로 클래스 간 결합도를 낮추고 응집도를 높이게 됨.

---

# 3. TDD의 핵심, 3단계 사이클 (순서)

> TDD는 다음 3개의 단계를 끊임없이 반복하며 완성됨. 이를 **Red-Green-Refactor** 사이클이라고 부름.
>
1. **🔴 Red (실패하는 테스트 작성):**
    - 아직 구현되지 않은 기능의 스펙(입출력, 예외)을 고민하며 테스트 코드 먼저 작성.
    - 실제 코드가 없으므로 테스트는 반드시 실패해야 함.
2. **🟢 Green (테스트 통과를 위한 최소 구현):**
    - 실패한 테스트를 통과시키기 위해 '가장 빠르고 단순하게' 코드를 작성함.
    - 이때는 하드코딩이나 다소 지저분한 코드도 허용됨. 오직 통과가 목적임.
3. **🔵 Refactor (리팩토링):**
    - 테스트가 통과하면, 기능 훼손 걱정 없이 코드를 깔끔하게 다듬음.
    - 중복을 제거하고 디자인 패턴을 적용하여 코드 품질을 높임.

---

# 4. 테스트 전략 (모든 것을 테스트하지 마라!!)

## **4-1. 테스트 코드를 짜는 것은 원래 오래 걸리고 귀찮은 작업이다.**

- TDD를 처음 도입하면 초기 코딩 시간은 프로덕션 코드만 짤 때보다 1.5배~2배 이상 늘어나며, 테스트 코드의 길이도 2~3배 길어지는 것이 지극히 정상임.
- 하지만 실무(현업)에서 이 엄청난 귀찮음을 감수하는 명확한 이유가 있음.

## 4-2. 시간 투자의 역설 (결국 개발 속도가 더 빨라짐)

- **디버깅 및 QA 시간 단축:** 배포 후 원인을 알 수 없는 버그를 찾기 위해 밤을 새우거나, Postman으로 수백 번 API를 수동 호출하며 "왜 안 넘어오지?" 고민하는 시간이 획기적으로 줄어듦.
- **심리적 안정감 (안전망):** 협업 시 다른 사람의 코드를 수정하거나 내 코드를 리팩토링할 때, 예기치 못한 부작용(Side-effect)으로 다른 기능이 망가지지 않았다는 것을 단 몇 초 만에 100% 증명받을 수 있음.
- **CI/CD 자동화의 필수 조건:** GitHub 등에 코드를 병합(Merge)하고 서버에 자동 배포(Deploy)하는 파이프라인을 구축할 때, 튼튼한 테스트 코드는 시스템이 터지는 것을 막아주는 최후의 보루임.

## 4-3. TDD 타협안: '선택과 집중'

[Test Pyramid software testing, AI로 생성](https://encrypted-tbn0.gstatic.com/licensed-image?q=tbn:ANd9GcSPYj3VEfqWO5Rdn0jRtJA9hUUDaiAn8rkwrHp7afh7L9q13OR2qJviBDGtIoiA4LpFtPY1T9LLOeetrr80riBDH66-2OXtXgE6yWwQCF45RMfEUKA)

테스트 피라미드: 빠르고 가벼운 단위 테스트(Service)를 아주 촘촘하게 짜고, 무겁고 느린 전체 통합 테스트는 필수 뼈대만 챙기기

### 실무에서도  '테스트 커버리지 100%'를 고집하는 회사는 드묾. 한정된 시간 안에서 효율성을 뽑아내기 위해 아래와 같이 타협함.

- **❌ 과감히 생략할 곳 (테스트 불필요):**
    - 단순 데이터 전달용 DTO 객체의 Getter/Setter
    - Spring Data JPA가 기본으로 제공하는 프레임워크 내부 메서드 (`save()`, `findById()` 등)
- **🔺 최소한만 검증할 곳 (핵심 뼈대만):**
    - Controller(웹 API)나 단순 Repository 계층.
    - 일어날 수 있는 모든 엣지 케이스를 쥐어짜기보다는, 정상적으로 통신이 이루어지는 **핵심 정상 흐름(Happy Path)** 1~2개 위주로만 가볍게 작성함.
- **🔥 에너지를 100% 쏟을 곳 (집중 타격):**
    - 애플리케이션의 **핵심 비즈니스 로직(Service 계층)**과 **도메인 규칙(Entity)**.
    - 돈이 오가는 결제 계산, 상품 재고 차감, 복잡한 if-else 조건문이 꼬여있는 곳 등 **'버그가 터지면 회사 서비스에 치명적인 타격을 주는 곳'**에 테스트 작성을 집중함.

---

# 5. TDD 기반 백엔드 개발 순서

> 스프링 부트 환경은 계층(Layer)별 역할이 명확히 분리되어 있음.
핵심 도메인부터 바깥쪽 웹 계층으로 확장해 나가는 **인사이드 아웃(Inside-Out)** 방식으로 진행함.
>

## 5-1. Domain / Entity 계층 (순수 자바 단위 테스트)

- **목표:** 프레임워크(Spring)나 DB 없이, 순수 자바 객체 스스로 상태를 관리하고 비즈니스 규칙을 지키는지 검증함.
- **TDD 실습 팁:** `도메인/Entity 클래스` 없이 테스트 코드를 먼저 작성. 이때, 발생하는 **컴파일 에러(빨간줄) 상태에서 IDE의 자동 생성 단축키를 활용해 클래스와 메서드의 뼈대를 만듦.** 사용자(객체를 호출하는 쪽) 중심 설계의 핵심임.
- 예시

    ```java
    // 1. 테스트 코드 (Red -> Green)
    class ProductTest {
        @Test
        @DisplayName("재고 차감 성공: 요청 수량만큼 재고가 줄어든다.")
        void decreaseStock_Success() {
    		    // given (준비)
            Product product = new Product("맥북", 10);
            
            // when (실행)
            product.decreaseStock(3);
            
            // then (검증)
            assertEquals(7, product.getStock());
        }
    
        @Test
        @DisplayName("재고 차감 실패: 현재 재고보다 많은 수량을 차감하면 예외가 발생한다.")
        void decreaseStock_ThrowsException() {
    		    // given (준비)
            Product product = new Product("맥북", 10);
            
            // when & then (실행 및 검증)
            assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(11));
        }
    }
    ```

    ```java
    // 2. 실제 프로덕션 코드 (통과를 위한 최소 구현)
    @Entity
    public class Product {
        @Id @GeneratedValue
        private Long id;
        private String name;
        private int stock;
    
        protected Product() {}
        public Product(String name, int stock) { this.name = name; this.stock = stock; }
        public int getStock() { return stock; }
    
        // 핵심 비즈니스 로직 (Setter 대신 도메인 언어 사용)
        public void decreaseStock(int quantity) {
            if (this.stock < quantity) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            this.stock -= quantity;
        }
    }
    ```


## 5-2. Repository 계층 (데이터 접근 테스트)

- **목표:** 작성한 엔티티가 실제 DB에 잘 저장되는지, 복잡한 커스텀 조회 쿼리가 의도대로 동작하는지 확인함.
- **특징:** `@DataJpaTest`를 사용해 가벼운 인메모리 DB(H2) 또는 Local DB 를 띄워 빠르고 격리된 환경에서 테스트함. 기본 제공되는 `save()`, `findById()` 등은 검증을 생략하고 직접 작성한 메서드만 테스트함.
- 예시 1 (JPA)

    ```java
    @DataJpaTest
    class ProductRepositoryTest {
    
        @Autowired
        private ProductRepository productRepository;
    
        @Test
        @DisplayName("이름으로 상품을 조회할 수 있다.")
        void findByName_Success() {
            // given (준비)
            productRepository.save(new Product("맥북", 10));
    
            // when (실행)
            Optional<Product> result = productRepository.findByName("맥북");
    
            // then (검증)
            assertTrue(result.isPresent());
            assertEquals(10, result.get().getStock());
        }
    }
    
    ```

    ```java
    // 프로덕션 코드
    public interface ProductRepository extends JpaRepository<Product, Long> {
        Optional<Product> findByName(String name); // 커스텀 쿼리 메서드
    }
    ```

- 예시 2 (JPA + MyBatis)

    ```java
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    import jakarta.persistence.EntityManager;
    
    import static org.junit.jupiter.api.Assertions.*;
    
    @DataJpaTest // JPA 관련 빈(EntityManager, Repository) 로드 및 트랜잭션 롤백 지원
    @AutoConfigureMybatis // MyBatis Mapper 빈 로드
    class BoardRepositoryTest {
    
        @Autowired
        private BoardJpaRepository boardJpaRepository; // JPA (CUD 담당)
    
        @Autowired
        private BoardMapper boardMapper; // MyBatis (Read 담당)
    
        @Autowired
        private EntityManager em; // 영속성 컨텍스트 제어용
    
        @Test
        @DisplayName("JPA로 게시글을 저장하고, MyBatis로 해당 게시글을 조회할 수 있다.")
        void saveWithJpa_readWithMyBatis_Success() {
            // given: JPA를 이용한 데이터 저장 (Create)
            Board board = new Board("테스트 제목", "테스트 내용");
            boardJpaRepository.save(board);
    
            // (중요) JPA의 쓰기 지연 SQL을 DB에 강제 반영하고 1차 캐시를 비움
            em.flush();
            em.clear();
    
            // when: MyBatis Mapper를 이용한 데이터 조회 (Read)
            // (실제 실무에서는 DTO로 조회하는 복잡한 쿼리가 들어감)
            BoardReadDto foundBoard = boardMapper.findById(board.getId());
    
            // then: 데이터 검증
            assertNotNull(foundBoard);
            assertEquals("테스트 제목", foundBoard.getTitle());
        }
    }
    ```


## 5-3. Service 계층 (비즈니스 흐름 테스트 - ⭐️가장 중요⭐️)

- **목표:** 애플리케이션의 핵심 비즈니스 흐름(조회 ➔ 검증 ➔ 도메인 로직 호출 ➔ 저장)을 검증함.
- **특징:** DB를 띄우지 않고, **Mockito** 프레임워크를 사용해 Repository를 가짜 객체(Mock)로 대체함. 오직 Service 로직 자체에만 집중함.
- 예시

    ```java
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import java.util.Optional;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.mockito.Mockito.*;
    
    @ExtendWith(MockitoExtension.class)
    class ProductServiceTest {
    
        @Mock
        private ProductRepository productRepository; // 가짜 DB 객체
    
        @InjectMocks
        private ProductService productService; // 테스트 대상
    
        @Test
        @DisplayName("상품 재고 차감 비즈니스 흐름이 정상 동작한다.")
        void decreaseProductStock_Success() {
            // given: 가짜 엔티티 준비 및 Mock 객체 행동 정의
            Product mockProduct = new Product("맥북", 10);
            when(productRepository.findByName("맥북")).thenReturn(Optional.of(mockProduct));
            
            // 추가된 부분: Service로 넘겨줄 요청 DTO 생성
            StockDecreaseRequest request = new StockDecreaseRequest("맥북", 3);
    
            // when: DTO를 파라미터로 전달
            productService.decreaseStock(request);
    
            // then: 도메인 로직이 정상 수행되어 7개가 남았는지 검증
            assertEquals(7, mockProduct.getStock());
            verify(productRepository, times(1)).findByName("맥북"); 
        }
    }
    ```

    ```java
    // DTO
    public class StockDecreaseRequest {
        private String name;
        private int quantity;
    
        public StockDecreaseRequest() {}
        public StockDecreaseRequest(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
    }
    ```

    ```java
    // 프로덕션 코드
    @Service
    @Transactional
    public class ProductService {
        private final ProductRepository productRepository;
        
        public ProductService(ProductRepository productRepository) {
            this.productRepository = productRepository;
        }
    
        // 파라미터로 DTO 객체를 직접 받도록 수정
        public void decreaseStock(StockDecreaseRequest request) {
            Product product = productRepository.findByName(request.getName())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            
            // 엔티티 내부의 도메인 로직 호출
            product.decreaseStock(request.getQuantity());
        }
    }
    ```


## 5-4. Controller 계층 (웹 API 통합 테스트)

- **목표:** 웹 계층(URL 매핑, HTTP 메서드, JSON 변환, HTTP 상태 코드)이 API 스펙대로 동작하는지 검증함.
- **특징:** `@WebMvcTest`와 `MockMvc`를 활용해 웹 계층만 떼어내어 가볍게 테스트함. Service 계층은 가짜(MockBean)로 만들어 웹 통신 자체의 정상 작동 여부에 집중함.
- 예시

    ```java
    @WebMvcTest(ProductController.class)
    class ProductControllerTest {
    
        @Autowired
        private MockMvc mockMvc;
    
        @MockBean
        private ProductService productService;
    
        @Autowired
        private ObjectMapper objectMapper;
    
        @Test
        @DisplayName("재고 차감 API 호출 시 200 OK 상태코드를 반환한다.")
        void decreaseStockApi_Success() throws Exception {
            // given: 이름("맥북")과 수량(3)을 모두 JSON 바디 객체에 담음
            StockDecreaseRequest request = new StockDecreaseRequest("맥북", 3);
            
            doNothing().when(productService).decreaseStock(eq("맥북"), anyInt());
    
            // when & then: 변경된 URL(/api/products/decrease)로 가짜 POST 요청 전송
            mockMvc.perform(post("/api/products/decrease")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
    ```

    ```java
    // 프로덕션 코드
    @RestController
    @RequestMapping("/api/products")
    public class ProductController {
        private final ProductService productService;
    
        public ProductController(ProductService productService) {
            this.productService = productService;
        }
    
        @PostMapping("/decrease") // 경로 변수({name}) 제거됨
        public ResponseEntity<Void> decreaseStock(@RequestBody StockDecreaseRequest request) {
            // request 안의 이름과 수량을 꺼내서 Service로 전달함
            productService.decreaseStock(request.getName(), request.getQuantity());
            return ResponseEntity.ok().build();
        }
    }
    ```


## 5-5. 전체 통합 테스트 (Integration Test)

- 프론트엔드 연동 직전, 가짜 객체 없이 `@SpringBootTest`로 전체 시스템(Controller ➔ Service ➔ DB)의 데이터 흐름을 최종 확인함.
- 예시

    ```java
    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    class ProductIntegrationTest {
    
        @Autowired
        private MockMvc mockMvc;
    
        @Autowired
        private ObjectMapper objectMapper;
    
        @Autowired
        private ProductRepository productRepository;
    
        @Test
        @DisplayName("상품 재고 차감 전체 흐름이 정상적으로 동작하고 DB에 반영된다.")
        void decreaseStock_Integration_Success() throws Exception {
            // 1. Given (실제 DB에 초기 데이터 저장)
            Product product = new Product("맥북", 10);
            productRepository.save(product);
            
            // 프론트엔드에서 제출할 JSON 데이터 (이름, 수량 포함)
            StockDecreaseRequest request = new StockDecreaseRequest("맥북", 3);
    
            // 2. When (변경된 URL로 실제 엔드포인트 호출)
            mockMvc.perform(post("/api/products/decrease")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    
                    // 3. Then (HTTP 상태 코드 검증)
                    .andExpect(status().isOk());
    
            // 4. Then (실제 DB에 7개로 업데이트되었는지 최종 확인)
            Product updatedProduct = productRepository.findByName("맥북").orElseThrow();
            assertEquals(7, updatedProduct.getStock(), "DB의 최종 재고가 7개여야 함");
        }
    }
    ```