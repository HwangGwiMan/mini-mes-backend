# Spring Modulith 리팩토링 계획

## 배경

현재 프로젝트는 `spring-modulith-starter-core` / `spring-modulith-starter-jpa` 의존성을 포함하고 있으나,
도메인 간 JPA 엔티티 직접 참조 및 서비스 간 직접 호출이 존재하여 실질적인 모듈 경계가 강제되지 않는 상태다.
이 계획은 Spring Modulith의 모듈 검증 기능을 실제로 통과하는 구조로 점진적으로 전환하는 것을 목표로 한다.

---

## 현재 위반 사항

| # | 위반 내용 | 위치 |
|---|-----------|------|
| V1 | `SalesOrder` 엔티티가 `Quote`, `Partner`, `Employee` 엔티티를 `@ManyToOne`으로 직접 참조 | `salesorder/domain/SalesOrder.java` |
| V2 | `Quote` 엔티티가 `Partner`, `Employee` 엔티티를 `@ManyToOne`으로 직접 참조 | `quote/domain/Quote.java` |
| V3 | `SalesOrderLine` 엔티티가 `Item` 엔티티를 `@ManyToOne`으로 직접 참조 | `salesorder/domain/SalesOrderLine.java` |
| V4 | `QuoteLine` 엔티티가 `Item` 엔티티를 `@ManyToOne`으로 직접 참조 | `quote/domain/QuoteLine.java` |
| V5 | `SalesOrderService`가 `QuoteRepository`, `PartnerRepository`, `EmployeeRepository`, `ItemRepository`를 직접 주입 | `salesorder/application/SalesOrderService.java` |
| V6 | `SalesOrderService.convertFromQuote()`에서 `Quote` 엔티티의 상태를 직접 변경 (`quote.updateStatus(...)`) | `salesorder/application/SalesOrderService.java` |
| V7 | `DataInitializer`가 `commoncode`, `auth` 도메인의 내부 클래스를 직접 import | `common/config/DataInitializer.java` |
| V8 | `infrastructure` 패키지가 `internal`로 선언되지 않아 외부 접근이 차단되지 않음 | 전 도메인 |

---

## 리팩토링 단계

### Step 1. 모듈 검증 테스트 추가 (기준선 확립)

**목적**: 현재 위반 목록을 테스트로 출력하여 리팩토링 진행 상황을 객관적으로 추적한다.

**작업 내용**:
- `ApplicationModulesTest` 테스트 클래스 작성
  ```java
  ApplicationModules.of(MiniMesBackendApplication.class).verify();
  ```
- 이 테스트는 Step 5 완료 전까지 실패 상태가 정상이다. 실패 메시지를 통해 남은 위반 항목을 확인한다.

**변경 파일**: `src/test/.../ApplicationModulesTest.java` (신규)

---

### Step 2. `infrastructure` → `internal` 패키지 이동

**목적**: Spring Modulith는 `internal` 서브패키지를 모듈 외부에서 접근할 수 없도록 자동 차단한다.
현재 `*QueryRepository`들은 모듈 내부 구현체이므로 외부에 노출될 필요가 없다.

**작업 내용**:
- 각 도메인의 `infrastructure` 패키지를 `internal`로 rename
  - `commoncode/infrastructure` → `commoncode/internal`
  - `employee/infrastructure` → `employee/internal`
  - `item/infrastructure` → `item/internal`
  - `partner/infrastructure` → `partner/internal`
  - `process/infrastructure` → `process/internal`
  - `quote/infrastructure` → `quote/internal`
  - `salesorder/infrastructure` → `salesorder/internal`
- 각 Service에서의 import 경로 수정

**변경 파일**: `*QueryRepository.java` (7개) + 이를 주입받는 `*Service.java` (7개)

---

### Step 3. `package-info.java` 추가

**목적**: 각 모듈의 경계를 코드로 명시한다.

**작업 내용**:
- 각 도메인 루트 패키지에 `package-info.java` 작성
  ```java
  @org.springframework.modulith.ApplicationModule
  package com.github.gwiman.mini_mes_backend.{domain};
  ```
- 대상 도메인: `auth`, `commoncode`, `employee`, `item`, `partner`, `process`, `quote`, `salesorder`
- `common` 패키지는 공유 유틸이므로 모듈로 선언하지 않고 공유 커널(shared kernel)로 유지

**변경 파일**: `package-info.java` (8개, 신규)

---

### Step 4. 크로스 도메인 JPA 참조 제거 — ID 참조로 전환 (V1~V4)

**목적**: 모듈 간 JPA 엔티티 직접 참조를 제거한다. 읽기(jOOQ 조인)는 이미 ID 기반이므로 쓰기 경로만 변경한다.

**작업 내용**:

#### 4-1. `Quote` 엔티티
```java
// 변경 전
@ManyToOne private Partner partner;
@ManyToOne private Employee employee;

// 변경 후
@Column(name = "partner_id") private Long partnerId;
@Column(name = "employee_id") private Long employeeId;
```
- `Quote.update()`, `Quote` 생성자 시그니처 변경
- `QuoteService`에서 엔티티 로드 후 넘기던 방식 → ID를 바로 저장하는 방식으로 변경

#### 4-2. `QuoteLine` 엔티티
```java
// 변경 전
@ManyToOne private Item item;

// 변경 후
@Column(name = "item_id") private Long itemId;
```

#### 4-3. `SalesOrder` 엔티티
```java
// 변경 전
@ManyToOne private Partner partner;
@ManyToOne private Employee employee;
@ManyToOne private Quote quote;

// 변경 후
@Column(name = "partner_id") private Long partnerId;
@Column(name = "employee_id") private Long employeeId;
@Column(name = "quote_id") private Long quoteId;
```
- `SalesOrder.update()`, 생성자 시그니처 변경

#### 4-4. `SalesOrderLine` 엔티티
```java
// 변경 전
@ManyToOne private Item item;

// 변경 후
@Column(name = "item_id") private Long itemId;
```

**주의**: `convertFromQuote()`에서 `quoteLine.getItem()`으로 Item 엔티티를 가져오는 부분은 `quoteLine.getItemId()`로 교체한다.

**변경 파일**: `Quote.java`, `QuoteLine.java`, `SalesOrder.java`, `SalesOrderLine.java`, `QuoteService.java`, `SalesOrderService.java`

---

### Step 5. 크로스 모듈 서비스 호출 제거 — Application Event 전환 (V5, V6)

**목적**: `SalesOrderService`가 다른 모듈의 Repository를 직접 주입받는 것을 제거한다.

**작업 내용**:

#### 5-1. 외래 모듈 Repository 주입 제거 (V5)
- Step 4 이후 `SalesOrderService`에서 `PartnerRepository`, `EmployeeRepository`, `ItemRepository` 주입이 불필요해진다 (ID만 저장하므로).
- `QuoteRepository` 주입은 Step 5-2에서 제거한다.

#### 5-2. `convertFromQuote()` — Quote 상태 변경을 Event로 분리 (V6)

이벤트 클래스 정의 (`salesorder` 모듈):
```java
public record QuoteConvertedToOrderEvent(Long quoteId) {}
```

발행 (`SalesOrderService`):
```java
applicationEventPublisher.publishEvent(new QuoteConvertedToOrderEvent(quoteId));
```

수신 (`quote` 모듈, `QuoteEventHandler` 신규):
```java
@ApplicationModuleListener
public void on(QuoteConvertedToOrderEvent event) {
    Quote quote = quoteRepository.findById(event.quoteId()).orElseThrow();
    quote.updateStatus("QUOTE_STATUS_05");
}
```

`convertFromQuote()`에서 quote의 lines를 읽어 SalesOrderLine을 구성하는 부분은,
Quote 라인 데이터를 별도 DTO(`QuoteLineData`)로 조회하는 `QuoteService.getLines(Long quoteId)` 공개 메서드를 통해 처리한다.

**변경 파일**:
- `salesorder/application/SalesOrderService.java`
- `quote/application/QuoteService.java` (공개 메서드 추가)
- `quote/application/QuoteEventHandler.java` (신규)
- `salesorder/QuoteConvertedToOrderEvent.java` (신규, 모듈 루트 또는 `application` 패키지)

---

### Step 6. `DataInitializer` 리팩토링 (V7)

**목적**: `common` 패키지의 `DataInitializer`가 `auth`, `commoncode` 도메인 내부를 직접 참조하지 않도록 한다.

**작업 내용**:
- `DataInitializer`를 `common/config`에서 제거하고 각 도메인 `application` 서비스의 공개 메서드를 통해 초기화하도록 변경
- 또는 `DataInitializer`를 각 도메인의 `application` 패키지로 분리 이동 (각 모듈이 자신의 초기 데이터를 책임)

**변경 파일**: `DataInitializer.java` + 각 도메인 서비스

---

## 완료 기준

- `ApplicationModules.of(MiniMesBackendApplication.class).verify()` 테스트 통과
- 모든 도메인 간 JPA `@ManyToOne` 참조 제거 (같은 Aggregate 내부 제외)
- 모든 기존 API 테스트 정상 동작

---

## 작업 순서 요약

```
Step 1  모듈 검증 테스트 추가          ← 기준선, 즉시 작업 가능
Step 2  infrastructure → internal    ← 독립적, 즉시 작업 가능
Step 3  package-info.java 추가        ← Step 2 이후
Step 4  JPA 엔티티 ID 참조 전환        ← 가장 많은 변경, Step 3 이후
Step 5  Application Event 도입        ← Step 4 이후
Step 6  DataInitializer 리팩토링      ← 독립적, 언제든 가능
```
