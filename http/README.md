# KMS HTTP 테스트 폴더 안내

이 폴더는 KMS 프로젝트 기준으로 HTTP 테스트 파일을 역할과 순서에 맞게 정리한 구조입니다.

## 권장 실행 순서

1. `00_guide(가이드)/01_http-test-start(HTTP 테스트 시작).md`
2. `01_auth(인증)/01_login-token(로그인 토큰).http`
3. `04_admin(관리자)/01_tag-management(태그 관리).http`
4. `02_worker(작업자)/01_article-command(문서 작성 수정 삭제).http`
5. `02_worker(작업자)/02_article-revision-start(문서 수정본 시작).http`
6. `02_worker(작업자)/06_rejected-article-flow(반려 문서 흐름).http`
7. `03_approver(승인자)/01_teamleader-approval(팀장 승인 처리).http`
8. `03_approver(승인자)/02_departmentleader-approval(부서장 승인 처리).http`
9. `02_worker(작업자)/03_article-query(문서 조회).http`
10. `02_worker(작업자)/04_my-articles-query(내 문서 조회).http`
11. `02_worker(작업자)/05_bookmark(북마크).http`
12. `03_approver(승인자)/03_approval-query(승인 조회).http`
13. `04_admin(관리자)/02_article-management(문서 관리).http`
14. `05_negative(실패 케이스)/01_critical-negative-flow(핵심 실패 케이스).http`

## 폴더 설명

- `00_guide(가이드)`: 테스트 순서와 체크리스트
- `01_auth(인증)`: 로그인, 토큰 갱신, 로그아웃
- `02_worker(작업자)`: 작업자 문서 작성/조회 흐름
- `03_approver(승인자)`: 팀장/부서장 승인 및 승인용 조회
- `04_admin(관리자)`: 태그 관리, 관리자 문서 수정/삭제
- `05_negative(실패 케이스)`: 실패해야 정상인 시나리오

## 필수 변수

아래 전역 변수는 실행 전에 준비해야 합니다.

- `accessToken`
- `accessToken`
- `workerId`
- `teamLeaderId`
- `departmentLeaderId`
- `equipmentId`
- `tagId`
- `articleId`
- `draftArticleId`
- `employeeId`
- `validPendingArticleId`
- `validApprovedArticleId`
- `validRejectedArticleId`
- `validDraftArticleId`
- `otherWorkerId`
