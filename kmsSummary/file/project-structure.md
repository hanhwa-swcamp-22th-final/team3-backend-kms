# KMS 프로젝트 구조 정리

> 작성일: 2026-03-26

---

## 브랜치 상태 주의

현재 **detached HEAD** (3eaa0ca) 상태.
main 브랜치에 더 최신 내용이 있음.

```
현재 위치(HEAD)   → 패키지 뼈대만 존재
main 브랜치       → build.gradle 업그레이드 + application.yml DB설정 + DatabaseConnectionTest 포함
```

**작업 전 반드시 main 브랜치로 이동**
```bash
git checkout main
```

---

## 프로젝트 기본 정보

| 항목 | 값 |
|------|----|
| 그룹 | `com.ohgiraffers` |
| 아티팩트 | `team3backendkms` |
| Java | 21 |
| Spring Boot | 4.0.4 |
| 빌드 도구 | Gradle |
| DB | MySQL (`setodb`) |

---

## build.gradle 의존성 (main 브랜치 기준)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
runtimeOnly 'com.mysql:mysql-connector-j'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
developmentOnly 'org.springframework.boot:spring-boot-devtools'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

---

## application.yml (main 브랜치 기준)

```yaml
spring:
  application:
    name: team3-backend-admin
  profiles:
    include: db          # application-db.yml 별도 파일로 민감정보 분리 (.gitignore)
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none     # DB 직접 관리 (자동 생성 안 함)
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
    show-sql: true

mybatis:
  mapper-locations: classpath:mappers/**/*.xml
  type-aliases-package: com.ohgiraffers.team3backendkms
  configuration:
    map-underscore-to-camel-case: true
```

---

## 패키지 구조 (CQRS 패턴)

```
com.ohgiraffers.team3backendkms/
├── Team3BackendKmsApplication.java
│
├── auth/                            ← 인증 (미구현)
├── common/                          ← 공통 유틸 (미구현)
├── config/                          ← Spring 설정 (미구현)
├── exception/                       ← 예외처리 (미구현)
├── jwt/                             ← JWT (미구현)
│
└── kms/
    ├── command/                     ← 쓰기 (Create / Update / Delete)
    │   ├── application/
    │   │   ├── controller/          ← REST 컨트롤러
    │   │   ├── dto/
    │   │   │   ├── request/         ← 요청 DTO
    │   │   │   └── response/        ← 응답 DTO
    │   │   └── service/             ← 비즈니스 로직 (TDD 핵심 대상)
    │   ├── domain/
    │   │   ├── aggregate/           ← Entity + Enum (TDD 시작점)
    │   │   ├── repository/          ← Repository 인터페이스
    │   │   └── service/             ← 도메인 서비스
    │   └── infrastructure/
    │       ├── repository/          ← Repository 구현체
    │       └── service/             ← 인프라 서비스
    │
    └── query/                       ← 읽기 (Read)
        ├── controller/              ← 조회 컨트롤러
        ├── dto/
        │   ├── request/             ← 조회 요청 DTO
        │   └── response/            ← 조회 응답 DTO
        ├── mapper/                  ← MyBatis Mapper 인터페이스
        └── service/                 ← 조회 서비스
```

---

## KMS 관련 DB 테이블 (ERD 기준)

### knowledge_article (핵심 테이블)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `article_id` | BIGINT PK | |
| `author_id` | BIGINT FK | 작성자 (employee) |
| `approved_by` | BIGINT FK | 승인자 (employee) |
| `equipment_id` | BIGINT FK | 설비 |
| `file_group_id` | BIGINT FK | 첨부파일 그룹 |
| `article_title` | VARCHAR(255) | 5~200자 |
| `article_category` | ENUM | 장애조치 / 공정개선 / 설비운영 / 안전 기타 |
| `article_content` | TEXT | 50~10,000자 (HTML/마크다운) |
| `article_status` | ENUM | Draft / Pending / Approved / Rejected |
| `article_approval_opinion` | VARCHAR(500) | 승인 의견 (최대 500자) |
| `approved_at` | TIMESTAMP | 승인 일시 |
| `article_rejection_reason` | VARCHAR(500) | 반려 사유 (10~500자) |
| `article_deletion_reason` | VARCHAR(500) | 삭제 사유 (10~500자) |
| `deleted_at` | TIMESTAMP | 삭제 일시 |
| `is_deleted` | BOOLEAN | 소프트 딜리트 여부 |
| `view_count` | INT | 조회수 (기본값 0) |
| `created_at` / `updated_at` | TIMESTAMP | Audit |

### knowledge_edit_history

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `history_id` | BIGINT PK | |
| `article_id` | BIGINT FK | |
| `editor_id` | BIGINT FK | 수정자 |
| `article_previous_content` | TEXT | 수정 전 내용 |
| `article_new_content` | TEXT | 수정 후 내용 |
| `edited_at` | TIMESTAMP | |

### knowledge_tag / knowledge_article_tag

| 테이블 | 컬럼 |
|--------|------|
| `knowledge_tag` | tag_id, tag_name |
| `knowledge_article_tag` | tag_id, article_id (중간 테이블) |

---

## API 목록 (구현 우선순위)

### 1순위 — 바로 구현 가능

| Method | URL | 기능 |
|--------|-----|------|
| GET | `/api/kms/articles` | 지식 목록 조회 (카테고리·정렬 필터) |
| GET | `/api/kms/articles/{articleId}` | 지식 상세 조회 |
| POST | `/api/kms/articles` | 지식 문서 등록 (PENDING) |
| POST | `/api/kms/articles/drafts` | 지식 문서 임시저장 (DRAFT) |
| POST | `/api/kms/approval/{articleId}/approve` | 지식 승인 처리 |
| POST | `/api/kms/approval/{articleId}/reject` | 지식 반려 처리 |
| GET | `/api/kms/my/articles` | 내 지식 목록 조회 |
| GET | `/api/kms/my/articles/stats` | 내 지식 현황 통계 |
| PUT | `/api/kms/articles/{articleId}` | 내 지식 수정 제출 (PENDING) |
| PUT | `/api/kms/articles/{articleId}/draft` | 내 지식 수정 임시저장 |
| DELETE | `/api/kms/articles/{articleId}` | 내 지식 삭제 |
| GET | `/api/kms/articles/{articleId}/history` | 지식 수정 이력 조회 |

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
| GET | `/api/employees/{employeeId}/skill-gap/articles` | 관련 KMS 지식 조회 |

### 3순위 — 스키마 확장 필요 (신규 테이블)

| 기능 | 신규 테이블 |
|------|------------|
| 댓글 CRUD | `knowledge_comment` |
| 구독 등록/해제 | `knowledge_subscription` |
| 멘토링 신청/수락/거절 | `mentoring_request`, `mentoring` |
| AI 검토 | `knowledge_ai_review` |
| AI 스킬갭 분석 | `skill_gap_analysis` |
| 학습 추천/시작 | `learning_course`, `learning_record` |

---

## 에러 코드 체계

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
| APPROVAL_003 | Pending 상태 아닌 문서에 승인·반려·보류 불가 |
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

## TDD 대상 파일 경로

```
# 테스트 파일
src/test/java/com/ohgiraffers/team3backendkms/
├── kms/command/domain/aggregate/KnowledgeArticleTest.java       ← STEP 1
└── kms/command/application/service/KnowledgeArticleServiceTest.java  ← STEP 2

# 프로덕션 파일
src/main/java/com/ohgiraffers/team3backendkms/
└── kms/command/domain/aggregate/
    ├── ArticleStatus.java       ← Enum
    ├── ArticleCategory.java     ← Enum
    └── KnowledgeArticle.java    ← Entity + 비즈니스 로직
```
