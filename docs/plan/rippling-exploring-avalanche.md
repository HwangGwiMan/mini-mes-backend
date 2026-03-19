# 도메인 엔티티 단위 테스트 개발 계획

## Context

현재 테스트는 Spring Modulith 모듈 경계 검증(`contextLoads`)만 존재한다. 핵심 도메인의 상태 전이 로직, 금액 계산, 비즈니스 규칙 위반 예외 처리에 대한 기능 검증이 전무하다. 인프라(DB, Spring Context) 없이 순수 JUnit5로 실행 가능한 엔티티 단위 테스트를 먼저 작성하여 빠른 피드백 루프를 확보하는 것이 목적이다.

---

## 테스트 파일 구조

```
src/test/java/com/github/gwiman/mini_mes_backend/
├── quote/domain/
│   ├── QuoteTest.java
│   └── QuoteLineTest.java
├── salesorder/domain/
│   ├── SalesOrderTest.java
│   └── SalesOrderLineTest.java
├── shipment/domain/
│   ├── ShipmentTest.java
│   └── ShipmentLineTest.java
└── revenue/domain/
    ├── RevenueTest.java
    └── RevenueLineTest.java
```

---

## 테스트 케이스 명세

### 1. QuoteTest.java

**검증 대상**: `Quote.create()`, `canSubmit()`, `canApprove()`, `update()`, `updateStatus()`

| 테스트 메서드 | 검증 내용 |
|---|---|
| `create_초기상태는_작성중이어야한다` | `create()` 후 statusCode == "QUOTE_STATUS_01" |
| `canSubmit_작성중일때_true반환` | statusCode=QUOTE_STATUS_01 → `canSubmit()` == true |
| `canSubmit_반려상태일때_true반환` | statusCode=QUOTE_STATUS_04 → `canSubmit()` == true |
| `canSubmit_제출됨일때_false반환` | statusCode=QUOTE_STATUS_02 → `canSubmit()` == false |
| `canApprove_제출됨일때_true반환` | statusCode=QUOTE_STATUS_02 → `canApprove()` == true |
| `canApprove_작성중일때_false반환` | statusCode=QUOTE_STATUS_01 → `canApprove()` == false |
| `update_제출후수정시_예외발생` | updateStatus("QUOTE_STATUS_02") 후 `update()` 호출 → `BusinessRuleViolationException` |
| `update_작성중일때_정상수정` | statusCode=QUOTE_STATUS_01 → `update()` 정상 반영 |

### 2. QuoteLineTest.java

**검증 대상**: `QuoteLine.of()` 금액 계산

| 테스트 메서드 | 검증 내용 |
|---|---|
| `of_금액은_수량곱하기단가` | quantity=10, unitPrice=1000 → amount=10000 |
| `of_소수점수량_금액계산_정확도` | quantity=2.5, unitPrice=400 → amount=1000.0000 |

### 3. SalesOrderTest.java

**검증 대상**: `SalesOrder.fromQuote()`, `create()`

| 테스트 메서드 | 검증 내용 |
|---|---|
| `fromQuote_초기상태는_접수여야한다` | statusCode == "ORDER_STATUS_01" |
| `fromQuote_주문일은_오늘이어야한다` | orderDate == LocalDate.now() |
| `fromQuote_quoteId가_설정된다` | quoteId가 파라미터 값으로 설정 |

### 4. SalesOrderLineTest.java

**검증 대상**: `SalesOrderLine.of()` 금액 계산

| 테스트 메서드 | 검증 내용 |
|---|---|
| `of_금액은_수량곱하기단가` | quantity=5, unitPrice=2000 → amount=10000 |

### 5. ShipmentTest.java

**검증 대상**: `Shipment.complete()`

| 테스트 메서드 | 검증 내용 |
|---|---|
| `complete_출하일_설정된다` | `complete(date)` 후 shipmentDate == date |
| `complete_상태코드가_완료로변경된다` | `complete(date)` 후 statusCode == "SHIPMENT_STATUS_03" |
| `complete_출하일null이면_예외발생` | `complete(null)` → NullPointerException 또는 BusinessRuleViolationException (실제 코드 확인 후 조정) |

> ⚠️ `complete(null)` 처리는 실제 Shipment.java 코드를 구현 시 확인 필요.

### 6. ShipmentLineTest.java

**검증 대상**: `ShipmentLine.complete()`, `updatePlan()`

| 테스트 메서드 | 검증 내용 |
|---|---|
| `complete_실제수량과금액이_기록된다` | actualQuantity, actualAmount 설정 확인 |
| `updatePlan_계획수량변경시_금액갱신` | plannedQuantity/plannedAmount 갱신 확인 |

### 7. RevenueTest.java

**검증 대상**: `Revenue.close()`, `Revenue.cancel()`

| 테스트 메서드 | 검증 내용 |
|---|---|
| `close_상태코드가_마감으로변경된다` | `close()` 후 statusCode == "REVENUE_STATUS_02" |
| `cancel_상태코드가_취소로변경된다` | `cancel()` 후 statusCode == "REVENUE_STATUS_03" |
| `update_마감후수정시_동작확인` | Service 레이어에서 막으므로 엔티티 메서드 자체는 상태 변경만 하면 됨 (코드 확인 후 조정) |

### 8. RevenueLineTest.java

**검증 대상**: `RevenueLine.update()` 금액 재계산

| 테스트 메서드 | 검증 내용 |
|---|---|
| `update_금액이_수량곱하기단가로_재계산된다` | quantity=3, unitPrice=5000 → amount=15000 |

---

## 구현 패턴

### 픽스처 헬퍼 (각 테스트 클래스 내 private static 메서드)

```java
// QuoteTest 내부 예시
private static Quote createDraftQuote() {
    return Quote.create("QT_202603_001", LocalDate.now(), LocalDate.now().plusDays(30),
        1L, 1L, 1L, "테스트 견적");
}
```

각 테스트 클래스에서 `@BeforeEach` 없이 private static 픽스처 메서드를 사용하여 테스트 간 독립성 유지.

### 예외 검증

```java
assertThatThrownBy(() -> quote.update(...))
    .isInstanceOf(BusinessRuleViolationException.class);
```

`org.assertj.core.api.Assertions.assertThatThrownBy` 사용 (Spring Boot Test에 기본 포함).

---

## 핵심 파일 경로

| 파일 | 역할 |
|---|---|
| `src/main/java/.../quote/domain/Quote.java` | 상태 전이 메서드, `BusinessRuleViolationException` 발생 |
| `src/main/java/.../quote/domain/QuoteLine.java` | `of()` 팩토리, amount 계산 |
| `src/main/java/.../salesorder/domain/SalesOrder.java` | `fromQuote()` 팩토리 |
| `src/main/java/.../salesorder/domain/SalesOrderLine.java` | `of()` 팩토리, amount 계산 |
| `src/main/java/.../shipment/domain/Shipment.java` | `complete()` — shipmentDate + SHIPMENT_STATUS_03 |
| `src/main/java/.../shipment/domain/ShipmentLine.java` | `complete()`, `updatePlan()` |
| `src/main/java/.../revenue/domain/Revenue.java` | `close()` → REVENUE_STATUS_02, `cancel()` → REVENUE_STATUS_03 |
| `src/main/java/.../revenue/domain/RevenueLine.java` | `update()` amount 재계산 |
| `src/main/java/.../common/exception/BusinessRuleViolationException.java` | 예외 클래스 |

---

## 의존성 추가 불필요

순수 JUnit5 + AssertJ만 사용. Spring Boot Test 스타터에 이미 포함.

> jqwik, Fixture Monkey 등 외부 테스트 라이브러리는 Layer 2 서비스 통합 테스트 단계에서 Fixture Monkey와 함께 검토.

---

## 검증 방법

```bash
# 단위 테스트만 실행 (DB 불필요, 빠름)
./gradlew test --tests "*.domain.*Test"

# 또는 특정 클래스
./gradlew test --tests "com.github.gwiman.mini_mes_backend.quote.domain.QuoteTest"
```

모든 테스트가 인프라 없이 통과해야 한다.
