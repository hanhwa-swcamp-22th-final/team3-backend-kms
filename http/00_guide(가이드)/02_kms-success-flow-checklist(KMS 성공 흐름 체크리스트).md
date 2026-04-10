# KMS 성공 흐름 체크리스트

이 문서는 실제 HTTP 테스트를 할 때 "어떤 요청이 성공해야 하는지"만 빠르게 보는 체크리스트입니다.

## 1. 사전 준비

1. 로그인으로 `accessToken`, `refreshToken` 발급
2. `workerId`, `teamLeaderId`, `departmentLeaderId`, `adminId` 확인
3. `equipmentId`, `tagId`, `articleId`, `draftArticleId` 등 상단 변수 확인

## 2. 성공 흐름 한눈에 보기

### 태그 준비

- `GET /api/kms/tags`
- `POST /api/kms/admin/articles/tags`
- `PUT /api/kms/admin/articles/tags`
- `DELETE /api/kms/admin/articles/tags`

### 작업자 작성 흐름

- `POST /api/kms/articles`
  기대: `201`, 승인 대기 상태로 등록
- `POST /api/kms/articles/drafts`
  기대: `201`, 초안 ID 생성
- `PUT /api/kms/articles/{draftArticleId}`
  기대: `200`, 초안 수정
- `PUT /api/kms/articles/{draftArticleId}/submit`
  기대: `200`, 초안 제출 후 승인 대기
- `PUT /api/kms/articles/{articleId}/tags`
  기대: `200`, 태그 연결

### 수정본 시작

- `PUT /api/kms/articles/{articleId}/revision`
  기대: `200`, 수정본용 새 draft ID 생성

### 반려 후 재작업

- `PUT /api/kms/articles/{articleId}`
  기대: `200`, 반려 문서 수정
- `PUT /api/kms/articles/{articleId}/submit`
  기대: `200`, 같은 문서 재제출

### 승인 처리

- `POST /api/kms/tl/articles/{articleId}/approval`
  기대: `200`, 팀장 승인/반려/보류
- `POST /api/kms/dl/articles/{articleId}/approval`
  기대: `200`, 부서장 승인/반려/보류

### 조회 확인

- `GET /api/kms/articles`
- `GET /api/kms/articles/{articleId}`
- `GET /api/kms/articles/contributors`
- `GET /api/kms/articles/recommendations`
- `GET /api/kms/tags`
- `GET /api/kms/articles?stat=approval`
- `GET /api/kms/articles/{articleId}?stat=approval`
- `GET /api/kms/stats?stat=approval`
- `GET /api/kms/my/articles`
- `GET /api/kms/my/articles/stats`
- `GET /api/kms/my/articles/history`
- `GET /api/kms/my/bookmarks`

### 북마크

- `POST /api/kms/bookmarks`
  기대: `201`
- `DELETE /api/kms/bookmarks`
  기대: `200`

### 관리자 문서 관리

- `PUT /api/kms/admin/articles/{articleId}`
  기대: `200`
- `DELETE /api/kms/admin/articles/{articleId}`
  기대: `200`

## 3. 실패 케이스는 여기서 확인

실패해야 정상인 요청은 아래 파일에서 확인합니다.

- `05_negative(실패 케이스)/01_critical-negative-flow(핵심 실패 케이스).http`
