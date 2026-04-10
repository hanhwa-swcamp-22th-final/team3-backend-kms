# KMS HTTP 테스트 시작 가이드

## 1. 시작 전에 준비할 것

1. `01_auth(인증)/01_login-token(로그인 토큰).http`로 토큰을 발급합니다.
2. `workerId`, `teamLeaderId`, `departmentLeaderId`, `adminId`가 잡혀 있는지 확인합니다.
3. `equipmentId`, `tagId`, `articleId`, `draftArticleId` 같은 샘플 값이 필요한 파일은 상단 변수를 먼저 봅니다.

## 2. 가장 쉬운 읽는 방법

각 `.http` 파일의 요청 제목은 아래 형식으로 읽으면 됩니다.

- `[성공]`: 정상 응답이 나와야 하는 요청
- `[실패]`: 에러 응답이 나와야 정상인 요청
- `기대: 200/201/400/...`: 우선 볼 HTTP 상태
- `핵심 확인`: 응답 `message`나 상태 변화에서 꼭 확인할 내용

## 3. 추천 실행 순서

1. 인증
   `01_auth(인증)/01_login-token(로그인 토큰).http`
2. 관리자 태그 준비
   `04_admin(관리자)/01_tag-management(태그 관리).http`
3. 작업자 작성/임시저장/제출
   `02_worker(작업자)/01_article-command(문서 작성 수정 삭제).http`
4. 승인 완료 문서 수정본 시작
   `02_worker(작업자)/02_article-revision-start(문서 수정본 시작).http`
5. 반려 후 재작업
   `02_worker(작업자)/06_rejected-article-flow(반려 문서 흐름).http`
6. 팀장 승인/반려/보류
   `03_approver(승인자)/01_teamleader-approval(팀장 승인 처리).http`
7. 부서장 승인/반려/보류
   `03_approver(승인자)/02_departmentleader-approval(부서장 승인 처리).http`
8. 조회 확인
   `02_worker(작업자)/03_article-query(문서 조회).http`
   `02_worker(작업자)/04_my-articles-query(내 문서 조회).http`
   `03_approver(승인자)/03_approval-query(승인 조회).http`
9. 북마크
   `02_worker(작업자)/05_bookmark(북마크).http`
10. 관리자 수정/삭제
   `04_admin(관리자)/02_article-management(문서 관리).http`
11. 실패 케이스 검증
   `05_negative(실패 케이스)/01_critical-negative-flow(핵심 실패 케이스).http`

## 4. 주의할 점

1. KMS API는 대부분 `Authorization: Bearer {{accessToken}}`가 필요합니다.
2. 승인 처리 요청은 `X-Employee-Id` 헤더도 필요합니다.
3. 등록/제출은 제목, 본문, 장비 ID 검증을 통과해야 합니다.
4. `articleId`, `draftArticleId`, `tagId`는 응답값을 저장해서 다음 요청에 계속 재사용합니다.
5. 반려 문서는 삭제가 아니라 `수정 -> 재제출` 흐름으로 테스트합니다.
