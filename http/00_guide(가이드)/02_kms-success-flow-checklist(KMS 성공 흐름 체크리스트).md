# KMS API 성공 흐름 체크리스트

이 문서는 `team3-backend-kms/http` 기준으로, 현재 구현된 API를 실제 검증할 때 필요한 핵심 흐름을 순서대로 정리한 체크리스트입니다.

## 1. 사전 준비

1. `01_인증/01_로그인_토큰.http`로 토큰을 발급합니다.
2. `workerId`, `teamLeaderId`, `departmentLeaderId`, `equipmentId`, `employeeId`를 준비합니다.
3. 아래 흐름에서 생성된 `articleId`, `draftArticleId`, `tagId`는 전역 변수로 저장해 재사용합니다.

## 2. 기본 정상 흐름

### 2.1 태그 생성/수정/삭제

1. `POST /api/kms/admin/articles/tags`
2. `PUT /api/kms/admin/articles/tags`
3. `DELETE /api/kms/admin/articles/tags`

### 2.2 작업자 문서 작성 흐름

1. `POST /api/kms/articles`
2. `POST /api/kms/articles/drafts`
3. `PUT /api/kms/articles/{draftArticleId}`
4. `PUT /api/kms/articles/{draftArticleId}/submit`
5. `PUT /api/kms/articles/{articleId}/tags`
6. `PUT /api/kms/articles/{articleId}/revision`
7. 작성 중 취소 시 `DELETE /api/kms/articles/{draftArticleId}`
8. 임시저장을 반복할 때는 새 초안을 만들지 않고 같은 `draftArticleId`에 `PUT`을 여러 번 호출

### 2.3 반려 후 재작업 흐름

1. 승인자가 문서를 `REJECT` 처리
2. 작성자가 같은 `articleId`를 `PUT /api/kms/articles/{articleId}`로 수정
3. 작성자가 같은 `articleId`를 `PUT /api/kms/articles/{articleId}/submit`으로 재제출
4. 반려 문서는 현재 구현상 작업자가 직접 삭제할 수 없음

### 2.4 승인 처리 흐름

1. `POST /api/kms/tl/articles/{articleId}/approval`
2. `POST /api/kms/dl/articles/{articleId}/approval`
3. 요청 바디 `status`는 `APPROVE`, `REJECT`, `PENDING` 중 하나를 사용합니다.
4. 요청 헤더 `X-Employee-Id`를 함께 전달합니다.

### 2.5 조회 흐름

1. `GET /api/kms/articles`
2. `GET /api/kms/articles/{articleId}`
3. `GET /api/kms/articles/contributors`
4. `GET /api/kms/articles/recommendations`
5. `GET /api/kms/tags`
6. `GET /api/kms/articles?stat=approval`
7. `GET /api/kms/articles/{articleId}?stat=approval`
8. `GET /api/kms/stats?stat=approval`
9. `GET /api/kms/my/articles`
10. `GET /api/kms/my/articles/stats`
11. `GET /api/kms/my/articles/history`
12. `GET /api/kms/my/bookmarks`

### 2.6 북마크 흐름

1. `POST /api/kms/bookmarks`
2. `DELETE /api/kms/bookmarks`

### 2.7 관리자 문서 관리 흐름

1. `PUT /api/kms/admin/articles/{articleId}`
2. `DELETE /api/kms/admin/articles/{articleId}`

## 3. 반드시 확인할 실패 케이스

1. DRAFT가 아닌 문서 제출 시도
2. PENDING, APPROVED, REJECTED 문서 삭제 시도
3. 작성자 불일치 수정/삭제 시도
4. 제목, 본문, 설비 ID 검증 실패
5. 승인/반려 중복 처리 시도
6. 반려 사유 길이 부족, 관리자 삭제 사유 길이 부족
7. 중복 북마크 추가, 없는 북마크 삭제
8. 반려된 문서 삭제 시도
9. 작성 중 취소 후 삭제된 초안 재수정 시도
