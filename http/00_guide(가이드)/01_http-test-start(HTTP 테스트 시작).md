# KMS HTTP 테스트 시작 가이드

## 1. 먼저 준비할 값

1. 로그인으로 토큰 발급
2. `workerId`, `teamLeaderId`, `departmentLeaderId` 확인
3. 테스트용 `equipmentId` 준비
4. 필요하면 태그 생성 후 `tagId` 저장

## 2. 추천 순서

1. `01_auth(인증)/01_login-token(로그인 토큰).http`
2. `04_admin(관리자)/01_tag-management(태그 관리).http`
3. `02_worker(작업자)/01_article-command(문서 작성 수정 삭제).http`
4. `02_worker(작업자)/02_article-revision-start(문서 수정본 시작).http`
5. `03_approver(승인자)/01_teamleader-approval(팀장 승인 처리).http`
6. `03_approver(승인자)/02_departmentleader-approval(부서장 승인 처리).http`
7. `02_worker(작업자)/03_article-query(문서 조회).http`
8. `02_worker(작업자)/04_my-articles-query(내 문서 조회).http`
9. `02_worker(작업자)/05_bookmark(북마크).http`
10. `03_approver(승인자)/03_approval-query(승인 조회).http`
11. `04_admin(관리자)/02_article-management(문서 관리).http`
12. `05_negative(실패 케이스)/01_critical-negative-flow(핵심 실패 케이스).http`

## 3. 주의 사항

1. KMS API는 현재 인증 헤더가 필요합니다.
2. 문서 등록과 제출은 `title`, `content`, `equipmentId` 검증을 통과해야 합니다.
3. 조회 API 일부는 `requesterId`, `requesterRole`, `authorId`, `employeeId` 쿼리 파라미터를 요구합니다.
4. 승인 처리는 `/approve`, `/reject`가 아니라 `POST /api/kms/tl/articles/{articleId}/approval`, `POST /api/kms/dl/articles/{articleId}/approval` 방식입니다.
5. 승인 처리 요청에는 `X-Employee-Id` 헤더도 함께 전달해야 합니다.
6. `articleId`, `draftArticleId`, `tagId`는 응답값을 전역 변수로 저장해서 다음 요청에 재사용합니다.
7. 작성 중 취소는 별도 취소 API가 아니라 `DRAFT 상태 문서 삭제`로 처리합니다.
8. 반려된 문서는 현재 작업자가 직접 삭제할 수 없고, 수정 후 재제출 흐름으로 처리해야 합니다.
