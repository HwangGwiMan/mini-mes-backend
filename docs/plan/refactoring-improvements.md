# 리팩토링 및 개선 계획

> 분석일: 2026-03-11

---

## 개요

코드베이스 직접 분석을 통해 발견된 버그, 설계 불일치, 중복 코드, 누락 기능을 우선순위별로 정리한다.

---

## 🔴 High Priority — 즉시 수정

### 1. ✅ `QuoteEventHandler` `@Transactional` 누락 (버그) — 완료

**위치:** `quote/application/QuoteEventHandler.java:19-23`

```java
@ApplicationModuleListener
// @Transactional 없음!
public void on(QuoteConvertedToOrderEvent event) {
    quoteRepository.findById(event.quoteId())
        .ifPresent(quote -> quote.updateStatus(QUOTE_STATUS_ORDERED));
}
```

**문제:** 견적이 수주로 전환될 때 견적 상태(`QUOTE_STATUS_ORDERED`)가 DB에 반영되지 않을 수 있음.
**해결:** `@Transactional` 추가.

---

### 2. ✅ 예외 계층 부재 및 `GlobalExceptionHandler` 불완전 — 완료

**위치:** `common/exception/GlobalExceptionHandler.java`

**문제:**
- `IllegalArgumentException`이 "찾을 수 없음(404)"과 "잘못된 입력(400)" 두 경우에 혼용
- `IllegalStateException`(비즈니스 규칙 위반)이 HTTP 500으로 처리됨
- `DataIntegrityViolationException`, `EntityNotFoundException` 핸들러 없음

**해결:** 커스텀 예외 계층 도입
```
common/exception/
├── ResourceNotFoundException   → HTTP 404
├── ValidationException         → HTTP 400
└── BusinessRuleViolationException → HTTP 409
```
`GlobalExceptionHandler`에 각 예외 핸들러 추가 후 서비스 전체 적용.

---

### 3. ✅ `SalesOrderLineRequest` 유효성 검증 누락 — 완료

**위치:** `salesorder/api/dto/SalesOrderLineRequest.java`

**문제:** `QuoteLineRequest`에는 `@DecimalMin`, `@Size` 등이 있지만 `SalesOrderLineRequest`에는 없어 0 수량, 음수 단가 허용.
**해결:** `QuoteLineRequest`와 동일한 수준의 `@Valid` 제약 조건 추가.

---

### 4. ✅ `orElseThrow()` 메시지 없음 — 완료 (2번 작업 중 함께 처리)

**위치:**
- `QuoteService.java:125, 174`
- `SalesOrderService.java:77, 100, 155`

**문제:** 메시지 없는 `orElseThrow()` 호출 시 `NoSuchElementException`이 발생하여 원인 파악 불가.
**해결:**
```java
.orElseThrow(() -> new ResourceNotFoundException("견적을 다시 조회할 수 없습니다: " + id))
```

---

### 5. `SecurityContextHolder` 직접 접근 (Anti-pattern)

**위치:** `QuoteService.java:80, 195-197`

**문제:** null 체크 없음. 비동기 컨텍스트 또는 스케줄러에서 NPE 위험.
**해결:** Controller에서 `@AuthenticationPrincipal`로 사용자 정보를 주입받아 서비스 메서드 파라미터로 전달.

---

### 6. ✅ Like 패턴 이스케이프 중복 (7곳) — 완료

**위치:** `EmployeeService`, `ItemService`, `PartnerService`, `ItemPriceService`, `ProcessService`, `QuoteService`, `SalesOrderService` 각 서비스의 검색 메서드

**문제:** 동일한 SQL 와일드카드 이스케이프 로직이 7개 서비스에 복사되어 있음.
**해결:** `common` 패키지에 `QueryParamEscaper` 유틸 클래스 추출.

---

### 7. 문서 번호 생성 로직 중복

**위치:**
- `QuoteService.java:262-274` (`QT_yyyyMM_###`)
- `SalesOrderService.java:191-203` (`SO_yyyyMM_###`)

**문제:** 동일한 채번 알고리즘이 두 곳에 중복.
**해결:** `common` 패키지에 `DocumentNumberGenerator` 서비스 추출. prefix와 테이블/컬럼을 파라미터로 받는 범용 인터페이스 설계.

---

## 🟡 Medium Priority

### 8. 페이지네이션 없음

**위치:** `QuoteController.java:37-45`, `SalesOrderController.java:33-41` 외 전체 목록 API

**문제:** 모든 `getAll()` 엔드포인트가 무제한 조회. 운영 데이터 증가 시 성능 문제 직결.
**해결:** Spring Data `Pageable` 파라미터 추가, jOOQ 쿼리에 `LIMIT` / `OFFSET` 적용. 응답은 `Page<T>` 또는 커스텀 `PageResponse<T>`로 래핑.

---

### 9. Audit 필드 불일치

**현황:**

| 도메인 | createdAt | updatedAt | createdBy | updatedBy |
|---|:---:|:---:|:---:|:---:|
| `Quote` | ❌ | ❌ | ✅ | ❌ |
| `ItemPrice` | ✅ | ✅ | ❌ | ❌ |
| 기타 도메인 | ❌ | ❌ | ❌ | ❌ |

**해결:** `common/domain/BaseEntity.java` (`@MappedSuperclass`) 추출 후 JPA `@EntityListeners(AuditingEntityListener.class)` 적용.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
}
```

---

### 10. 낙관적 잠금 없음

**문제:** 동시 수정 시 나중 저장이 무조건 덮어씀. 견적, 수주 같은 중요 문서에서 데이터 유실 가능.
**해결:** 핵심 엔티티(`Quote`, `SalesOrder`)에 `@Version Long version` 필드 추가.

---

### 11. ✅ `QuoteApproval.comment` `@Size` 누락 — 완료 (이미 적용되어 있었음)

**위치:** `quote/domain/QuoteApproval.java:41` (DB `varchar(500)`), `quote/api/dto/ApprovalRequest.java`

**문제:** DTO에 `@Size(max=500)` 없어 500자 초과 시 DB 레벨 에러로만 처리됨.
**해결:** `ApprovalRequest`에 `@Size(max=500)` 추가.

---

### 12. 이벤트 발행 타이밍

**위치:** `SalesOrderService.java:153`

```java
SalesOrder saved = salesOrderRepository.save(order);
eventPublisher.publishEvent(new QuoteConvertedToOrderEvent(quoteId)); // 핸들러 실패 시 불일치
```

**문제:** 핸들러 실패 시 수주는 저장되고 견적 상태는 미변경 상태 가능.
**해결:** `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 검토 또는 동일 트랜잭션 내 처리 여부 결정.

---

### 13. 조회 방식 불일치

**문제:** 일부 도메인(Item, Partner, Process)은 커스텀 JPA 쿼리, 다른 도메인(Quote, SalesOrder)은 jOOQ 사용. 명시적 기준 없음.
**해결:** CLAUDE.md 원칙("복잡한 Read는 jOOQ") 재확인 후 단순 검색도 jOOQ로 통일하거나, 기준을 문서화.

---

## 🟢 Low Priority

### 14. 테스트 거의 전무

**현황:** `ApplicationModulesTest.java` 하나 (모듈 경계 검증만).

**누락된 테스트:**
- 서비스 비즈니스 로직 단위 테스트
- 견적 제출 → 승인 → 수주 전환 통합 테스트
- 채번 중복 방지 테스트
- 에러 시나리오 테스트

**해결:** 우선 `QuoteService`, `SalesOrderService` 핵심 플로우 테스트부터 작성.

---

### 15. 기본 비밀번호 하드코딩

**위치:** `employee/application/EmployeeService.java:26`

```java
private static final String DEFAULT_PASSWORD = "pw1234";
```

**문제:** 소스코드에 평문 노출.
**해결:** `application-local.yaml`에 설정값으로 분리하거나, 임시 랜덤 비밀번호 생성 후 최초 로그인 시 변경 강제.

---

## 작업 순서 (권장)

| 단계 | 항목 | 예상 범위 |
|---|---|---|
| 1 | ✅ `QuoteEventHandler` `@Transactional` 추가 | 완료 |
| 2 | ✅ 커스텀 예외 클래스 + `GlobalExceptionHandler` 보완 | 완료 |
| 3 | ✅ `SalesOrderLineRequest` 검증 추가 | 완료 |
| 4 | ✅ `orElseThrow()` 메시지 일괄 수정 | 완료 (2번에서 처리) |
| 5 | ✅ `QueryParamEscaper` 추출 (중복 7곳 제거) | 완료 |
| 6 | `DocumentNumberGenerator` 추출 | 소규모 |
| 7 | 페이지네이션 적용 | 대규모 |
| 8 | `BaseEntity` 추출 (Audit 필드 통일) | 중간 |
| 9 | 테스트 작성 | 대규모 |
