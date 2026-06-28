# TicketFlow

회사·유저·티켓 기반 사내 헬프데스크(티켓) 관리 서비스.
RESTful API + React. **표준 응답 규약(ApiResult)·Flyway·JWT·REST Docs** 등 엔지니어링 컨벤션 중심.

> 기술테스트 제출 프로젝트. 설계 문서: `docs/DESIGN.md` · 사용자 매뉴얼: `docs/MANUAL.md`

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
./gradlew test          # 단위(@Nested) + 통합(MockMvc) + RestDocs 스니펫 생성
./gradlew asciidoctor   # build/docs/asciidoc/index.html (테스트 기반 자동 API 문서)
```

## 핵심 컨벤션
`ApiResult<T>` 표준 응답 + `ErrorCode` 카탈로그 · package-by-feature 레이어드(controller/service/repository/domain/dto) · Flyway(V1 스키마/V2 시드, `ddl-auto=validate`) · BaseEntity+Auditing · JWT + `@PreAuthorize` · `@LoginUser` 회사 스코프 · 네이티브 커스텀 리포지토리 + DTO 매핑 · REST Docs.

## 테스트 구성
도메인 단위(`TicketStatusTest`) · 서비스 단위(Mockito, `*ServiceTest`) · API 통합(MockMvc + RestDocs, `*ApiTest`) 3계층, 총 36개.

## 주요 API
모든 응답은 `ApiResult<T>` 엔벨로프(`{success, data}` 또는 `{success, error:{code, message}}`).

| Method | Path | 설명 | 인가 |
|---|---|---|---|
| POST | `/api/auth/signup` · `/api/auth/login` | 회사 온보딩 · 로그인 | all |
| POST · GET | `/api/users` · `/api/users?role=` | 멤버 추가 · 목록(배정 후보) | ADMIN |
| POST · GET | `/api/tickets` · `/api/tickets?status=&assigneeId=` | 생성 · 목록(회사 스코프) | auth |
| GET | `/api/tickets/{id}` · `/api/tickets/stats` | 상세+코멘트 · 상태별 집계 | auth |
| PATCH | `/api/tickets/{id}/status` | 상태 전이(전이 규칙 검증) | auth |
| PATCH · DELETE | `/api/tickets/{id}/assignee` · `/api/tickets/{id}` | 담당자 배정 · 삭제 | ADMIN |
| POST | `/api/tickets/{id}/comments` | 코멘트 | auth |

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
설계 문서(7항목)와 사용자 매뉴얼은 제출 시 별도로 제공합니다.
