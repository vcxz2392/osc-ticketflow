# TicketFlow

회사·유저·티켓 기반 사내 헬프데스크(티켓) 관리 서비스.

## 기술 스택
- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, Spring Security(JWT), Flyway, Gradle
- **DB**: MySQL 8 (런타임, Docker) / H2 (테스트)
- **Frontend**: React + Vite
- **API 문서**: Spring REST Docs

## 실행 (한 번에)
```bash
docker compose up --build
# → http://localhost:5173  (MySQL + 백엔드 + 프론트 전체 기동, nginx 가 /api 프록시)
```
Flyway 가 스키마·시드를 자동 적용하므로 별도 DB 준비가 필요 없다.

<details><summary>개발 모드 (백엔드/프론트를 따로 띄울 때)</summary>

```bash
docker compose up -d mysql                  # DB만 (호스트 3306 노출)
cd backend && ./gradlew bootRun             # :8080
cd frontend && npm install && npm run dev   # :5173 (vite proxy → :8080)
```
</details>

### 데모 계정 (회사: Acme Corp)
| 계정 | 비밀번호 | 역할 |
|---|---|---|
| `admin1` | `admin123` | ADMIN (배정·삭제·멤버추가·전체조회) |
| `user1` | `user123` | USER (티켓 등록·본인 티켓) |

## 테스트 & API 문서
```bash
cd backend
./gradlew test          # 테스트 실행 + RestDocs 스니펫 생성
./gradlew asciidoctor   # build/docs/asciidoc/index.html (테스트 기반 자동 API 문서)
```
서비스 단위(Mockito, `*ServiceTest`)와 API 통합(MockMvc + RestDocs, `*ApiTest`)으로 구성했습니다.

## 핵심 컨벤션
`ApiResult<T>` 표준 응답 + `ErrorCode` 카탈로그 · package-by-feature 레이어드(controller/service/repository/domain/dto) · Flyway(V1 스키마/V2 시드, `ddl-auto=validate`) · BaseEntity+Auditing · JWT + `@PreAuthorize` · `@LoginUser` 회사 스코프 · 네이티브 커스텀 리포지토리 + DTO 매핑 · REST Docs.

## 구조 (package-by-feature 레이어드)
```
backend/src/main/java/com/ticketflow
├─ common/        response·exception·entity·security·config (공통)
├─ auth/          controller·service·dto
├─ user/          controller·service·repository·domain·dto
├─ company/       domain·repository
└─ ticket/        controller·service·repository(+네이티브 Impl)·domain·dto
frontend/         React(Vite) — 로그인·티켓목록·상세
```

## 문서
설계 문서와 사용자 매뉴얼은 제출 시 별도로 제공합니다.
