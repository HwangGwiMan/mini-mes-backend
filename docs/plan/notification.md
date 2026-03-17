# 사용자 알림(Notification) 기능 개발 계획

> 작성일: 2026-03-17

## 개요

MES 업무 이벤트 발생 시 담당 직원에게 실시간 알림을 전송하고, 알림 이력을 영속적으로 관리한다.
알림은 삭제되지 않으며 읽음 플래그(`isRead`)만 변경된다.

---

## 설계 결정

| 항목 | 결정 | 근거 |
|---|---|---|
| 수신자 식별 | `employeeId` (Long) | 도메인 이벤트가 이미 employeeId를 포함, 모든 사용자는 직원 계정을 가짐 |
| 실시간 전송 | SSE (`SseEmitter`) | 단방향 서버→클라이언트, 추가 의존성 없음 (webmvc 내장) |
| 알림 영속화 | `notification` 테이블 | 읽음 여부 관리, 미접속 시 이력 보존 |
| 읽음 처리 | `isRead` 플래그만 변경 | 알림 데이터는 삭제하지 않음 |
| SSE 구독 키 | `employeeId` | User 도메인 직접 의존 없이 직원 ID로 emitter 관리 |

---

## 도메인 구조

```
notification/
├── api/
│   ├── NotificationController.java
│   └── dto/
│       └── NotificationResponse.java
├── application/
│   ├── NotificationService.java
│   └── NotificationEventHandler.java
├── domain/
│   ├── Notification.java
│   └── NotificationRepository.java
└── package-info.java
```

---

## 엔티티 설계

```
notification 테이블
├── id                   BIGINT PK (IDENTITY)
├── recipient_employee_id BIGINT NOT NULL        ← 수신 담당자 (FK 없음, ID만 저장)
├── message              VARCHAR(500) NOT NULL
├── is_read              BOOLEAN NOT NULL DEFAULT FALSE
├── created_at           TIMESTAMP               ← BaseEntity 상속
├── updated_at           TIMESTAMP
├── created_by           VARCHAR(50)
├── updated_by           VARCHAR(50)
└── version              BIGINT
```

- `BaseEntity` 상속으로 audit 자동 적용
- `@Version` 낙관적 잠금 포함 (읽음 처리 동시성 대비)
- 도메인 규칙: `isRead = false`로만 생성, `markAsRead()`로만 변경

---

## API 설계

| Method | Path | 응답 | 설명 |
|---|---|---|---|
| `GET` | `/api/notifications/subscribe` | SSE stream | SSE 구독 (현재 로그인 사용자 기준) |
| `GET` | `/api/notifications` | `List<NotificationResponse>` | 내 알림 목록 (미읽음 우선 정렬) |
| `GET` | `/api/notifications/unread-count` | `{ "count": N }` | 미읽음 배지 수 조회 |
| `PATCH` | `/api/notifications/{id}/read` | `{ "unreadCount": N }` | 단건 읽음 처리 |
| `PATCH` | `/api/notifications/read-all` | `{ "unreadCount": 0 }` | 전체 읽음 처리 |

- 모든 API는 JWT 인증 필요
- `GET /api/notifications/subscribe` : SSE 연결 후 `employeeId`로 emitter 등록

### SSE 이벤트 페이로드

새 알림 발생 시 실시간으로 전송되는 SSE 데이터:

```json
{
  "id": 42,
  "message": "수주 SO_202503_001이 생성되었습니다.",
  "isRead": false,
  "createdAt": "2026-03-17T10:00:00",
  "unreadCount": 4
}
```

`unreadCount`를 포함해 클라이언트가 별도 API 호출 없이 배지를 즉시 갱신할 수 있다.

---

## 이벤트 연결 계획

### 1단계 (초기 구현)

| 이벤트 | 수신자 조회 방법 | 알림 메시지 |
|---|---|---|
| `SalesOrderCreatedEvent(salesOrderId)` | SalesOrderRepository로 employeeId 조회 | "수주 {orderNumber}이 생성되었습니다." |
| `QuoteConvertedToOrderEvent(quoteId)` | QuoteService로 employeeId 조회 | "견적 {quoteNumber}이 수주로 전환되었습니다." |

### 향후 확장 (출하 완료 등)

- `ShipmentCompletedEvent` 발행 후 동일 패턴으로 연결

---

## SSE 처리 흐름

```
[클라이언트 로그인 후]
  GET /api/notifications/subscribe
    → SecurityContext에서 User.employeeId 추출
    → SseEmitter 생성 후 ConcurrentHashMap<Long, SseEmitter>에 등록
    → 연결 종료/타임아웃 시 자동 제거

[이벤트 발행 시]
  NotificationEventHandler (@ApplicationModuleListener)
    → Notification 엔티티 생성 및 저장 (DB 영속화)
    → emitterMap에서 recipientEmployeeId로 emitter 조회
      → 있으면: SSE 즉시 전송
      → 없으면: DB 저장만 (재접속 시 목록 API로 확인)
```

---

## 구현 순서

### Step 1. 도메인 레이어
- [ ] `Notification.java` — 엔티티, `create()` 정적 팩토리, `markAsRead()` 메서드
- [ ] `NotificationRepository.java` — `findByRecipientEmployeeIdOrderByCreatedAtDesc()`

### Step 2. 애플리케이션 레이어
- [ ] `NotificationService.java` — SSE emitter 관리, 알림 저장 및 전송, 읽음 처리
- [ ] `NotificationEventHandler.java` — `@ApplicationModuleListener`로 이벤트 수신

### Step 3. API 레이어
- [ ] `NotificationResponse.java` — 응답 DTO
- [ ] `NotificationController.java` — SSE 구독, 목록 조회, 읽음 처리 엔드포인트

### Step 4. 모듈 선언
- [ ] `package-info.java` — `@ApplicationModule` 선언

### Step 5. 이벤트 연결
- [ ] `NotificationEventHandler`에 `SalesOrderCreatedEvent` 핸들러 추가
- [ ] `NotificationEventHandler`에 `QuoteConvertedToOrderEvent` 핸들러 추가

---

## 모듈 간 의존성 규칙

`notification` 모듈이 접근해도 되는 것:
- `salesorder.domain.SalesOrderRepository` — 수주 번호/담당자 조회용
- `salesorder.application.SalesOrderCreatedEvent` — 이벤트 수신
- `salesorder.application.QuoteConvertedToOrderEvent` — 이벤트 수신
- `quote.application.QuoteService` — 견적 번호/담당자 조회용

`notification` 모듈이 접근하면 안 되는 것:
- 타 모듈 `internal` 패키지
- `auth.domain.UserRepository` — User 도메인 직접 의존 금지 (employeeId로만 식별)

---

## 검증 방법

1. **단위**: `NotificationService` — emitter 등록/해제, 미접속 시 DB 저장만 되는지
2. **통합**: `SalesOrderService.create()` 호출 → `Notification` 레코드 생성 확인
3. **E2E**: SSE 구독 후 수주 생성 API 호출 → 실시간 이벤트 수신 확인
