# 작업 로그 - 2026-03-26 | 사전 준비

---

## 완료 내용

### 프로젝트 구조 파악
- main 브랜치 기준 build.gradle, application.yml, 패키지 구조 전체 확인
- DB: `setodb` (MySQL), JPA + MyBatis 혼용, `application-db.yml` 분리
- CQRS 패턴 적용 (command / query 분리)
- 기존 파일: `ApiResponse.java`, `DatabaseConnectionTest.java` 확인

### Git 정리
- detached HEAD → `main` 브랜치로 복귀
- `kmsSummary/` .gitignore 경로 수정 (`../kmsSummary/` → `kmsSummary/`)
- `git rm -r --cached kmsSummary/` 로 캐시 제거 완료
- `feat/kms-tdd-test` 브랜치 생성

### 문서 작성
- `kmsSummary/file/project-structure.md` — 프로젝트 전체 구조 정리
- `kmsSummary/file/kms-overview.md` — KMS 전체 개요 (테이블·API·에러코드)
- GitHub 이슈 내용 작성 완료

---

## 다음 작업

- STEP 1-1 🔴 Red — `KnowledgeArticleTest.java` 작성 시작
