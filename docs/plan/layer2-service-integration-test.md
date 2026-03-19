# Layer 2: 서비스 통합 테스트 개발 계획

## Context

Layer 1(도메인 단위 테스트)이 엔티티 메서드 자체를 검증한다면, Layer 2는 **실제 PostgreSQL + Spring Context**를 사용하여 서비스 레이어의 전체 유스케이스를 검증한다. 핵심 검증 대상은:

- 이벤트 체인: `SalesOrderCreatedEvent` → `ShipmentService.on()` → 출하 계획 자동 생성
- 견적 전환 흐름: Quote create → submit → approve → `convertFromQuote()` → 출하 자동 생성
- 문서 번호 연속 채번 (동월 내 순번 증가)
- 도메인 간 FK 검증 (`PartnerService.exists()`, `EmployeeService.exists()` 등)
- 서비스 레이어의 상태 전이 가드 (`BusinessRuleViolationException`)

---

## 의존성 추가 (`build.gradle`)

```groovy
// Testcontainers
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:postgresql'
testImplementation 'org.testcontainers:junit-jupiter'

// Fixture Monkey (Naver OSS — 복잡한 픽스처 셋업 간소화)
testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.3'
```

---

## 테스트 설정 파일

### `src/test/resources/application-test.yaml`

```yaml
spring:
  config:
    activate:
      on-profile: test
  jpa:
    hibernate:
      ddl-auto: create-drop   # 테스트마다 스키마 재생성
    show-sql: false
  # datasource는 @DynamicPropertySource로 주입

jwt:
  secret: dGVzdC1zZWNyZXQta2V5LW11c3QtYmUtMzJieXRlcw==
  expiration-ms: 86400000
```

---

## 기반 클래스

### `ServiceIntegrationTest.java` (abstract)

```java
@SpringBootTest(webEnvironment = NONE)
@Testcontainers
@ActiveProfiles("test")
@Transactional
abstract class ServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
}
```

> `@Transactional` 전략: 각 테스트가 트랜잭션으로 감싸여 롤백되므로 테스트 간 데이터 오염 없음. 단, 이벤트 체인 테스트(`SalesOrderCreatedEvent` → `ShipmentService.on()`)는 `@ApplicationModuleListener`가 **동일 트랜잭션**에서 동기 실행되므로 롤백 경계 내에서 검증 가능.

---

## Fixture Monkey 설정

### `FixtureMonkeySupport.java` (공통 픽스처 헬퍼)

```java
class FixtureMonkeySupport {

    static final FixtureMonkey fm = FixtureMonkey.builder()
        .plugin(new LombokPlugin())
        .plugin(new JakartaValidationPlugin())
        .build();
}
```

**Fixture Monkey 적용 범위**: 사전 데이터(Partner, Employee, Item 등) 생성 시 보일러플레이트 감소. 엔티티의 `@Size`, `@NotNull` 제약을 자동 반영. 단, 비즈니스 규칙이 있는 엔티티(Quote, SalesOrder 등)는 직접 팩토리 메서드 호출.

---

## 테스트 파일 구조

```
src/test/java/com/github/gwiman/mini_mes_backend/
├── support/
│   ├── ServiceIntegrationTest.java        ← 기반 클래스
│   └── FixtureMonkeySupport.java          ← Fixture Monkey 설정
├── quote/application/
│   └── QuoteServiceIntegrationTest.java
├── salesorder/application/
│   └── SalesOrderServiceIntegrationTest.java
├── shipment/application/
│   └── ShipmentServiceIntegrationTest.java
└── revenue/application/
    └── RevenueServiceIntegrationTest.java
```

---

## 테스트 케이스 명세

### Auth 선행 조건

`submit()`, `approve()`, `reject()`는 `username` 파라미터를 통해 `AuthService.findEmployeeIdByUsername()` 를 호출한다. 각 테스트에서 `AuthService.register()` (또는 `UserRepository.save()`)로 User 레코드를 생성하고, 대응하는 Employee 레코드의 ID와 연결해야 한다.

---

### 1. `QuoteServiceIntegrationTest`

**사전 데이터**: Partner, Employee(담당자), Employee(승인자), Item(복수), User(승인자 계정)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `create_견적번호_채번_및_저장` | `QT_yyyyMM_001` 형식, DB에 Quote + QuoteLine 저장 확인 |
| `create_동월_두번째_견적_순번증가` | 동월 두 번째 생성 시 `QT_yyyyMM_002` |
| `create_존재하지않는파트너_예외` | `ResourceNotFoundException` |
| `submit_제출_상태전이` | `QUOTE_STATUS_02`로 변경, 권한 있는 사용자 |
| `submit_이미제출된견적_예외` | `BusinessRuleViolationException` |
| `approve_승인_상태전이_및_이력저장` | `QUOTE_STATUS_03`, `QuoteApproval` 레코드 생성 확인 |
| `approve_승인자아닌사용자_예외` | `BusinessRuleViolationException` |
| `reject_반려_상태전이` | `QUOTE_STATUS_04` |
| `update_제출후수정_예외` | `submit()` 후 `update()` → `BusinessRuleViolationException` |

---

### 2. `SalesOrderServiceIntegrationTest`

**사전 데이터**: Partner, Employee, Item(복수), 승인 완료된 Quote

| 테스트 메서드 | 검증 내용 |
|---|---|
| `create_수주_생성_및_출하계획_자동생성` | `SalesOrderCreatedEvent` → `Shipment` 레코드 자동 생성, `ShipmentLine` 수 = `SalesOrderLine` 수 |
| `create_출하계획_계획수량_검증` | ShipmentLine의 plannedQuantity == SalesOrderLine의 quantity |
| `create_수주번호_채번` | `SO_yyyyMM_001` 형식 |
| `convertFromQuote_전체흐름` | Quote(승인완료) → 수주 생성 → `QuoteConvertedToOrderEvent` 발행 → Quote 상태 변화 → Shipment 자동 생성 |
| `convertFromQuote_미승인견적_예외` | `QUOTE_STATUS_01` 상태 → `BusinessRuleViolationException` |
| `convertFromQuote_중복전환_예외` | 동일 quoteId 두 번 전환 → `BusinessRuleViolationException` |

---

### 3. `ShipmentServiceIntegrationTest`

**사전 데이터**: SalesOrder + SalesOrderLine (이미 `createFromOrder()`로 Shipment가 생성된 상태 가정)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `createFromOrder_출하계획_생성` | `Shipment` 저장, `SHIPMENT_STATUS_01`, `SH_` 번호, ShipmentLine plannedAmount 계산 확인 |
| `complete_출하완료_처리` | shipmentDate 기록, `SHIPMENT_STATUS_03`, actualQuantity/actualAmount 반영 |
| `complete_이미완료된출하_예외` | `BusinessRuleViolationException` |
| `update_완료후수정_예외` | complete 후 update → `BusinessRuleViolationException` |
| `delete_대기상태만_삭제가능` | `SHIPMENT_STATUS_01` 삭제 성공, `SHIPMENT_STATUS_02` 삭제 → `BusinessRuleViolationException` |

---

### 4. `RevenueServiceIntegrationTest`

**사전 데이터**: 완료된 Shipment (findAvailableOrderLines() 대상)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `create_매출_생성_및_번호채번` | `RE_yyyyMM_001`, DB 저장, amount = quantity × unitPrice |
| `close_마감_상태전이` | `REVENUE_STATUS_02` |
| `cancel_취소_상태전이` | close() 후 cancel() → `REVENUE_STATUS_03` |
| `update_초안에서만_수정가능` | close 후 update → `BusinessRuleViolationException` |
| `delete_초안에서만_삭제가능` | close 후 delete → `BusinessRuleViolationException` |

---

## 핵심 설계 결정

### 이벤트 체인 검증 방식

`ShipmentService.on()` 은 `@ApplicationModuleListener`로 등록되어 동일 트랜잭션 내 동기 실행된다. 따라서 `salesOrderService.create()` 호출 후 같은 트랜잭션 안에서 `ShipmentRepository.findBySalesOrderId(orderId)`로 즉시 검증 가능하다.

### Fixture Monkey 사용 범위

| 사용 O | 사용 X |
|---|---|
| Partner, Employee, Item, User 사전 데이터 생성 | Quote, SalesOrder 등 비즈니스 팩토리 메서드가 있는 엔티티 |
| 복수 SalesOrderLine 목록 생성 | 상태 코드가 의미 있는 엔티티 (직접 생성) |

### `@Transactional` 예외

`convertFromQuote_전체흐름` 테스트처럼 **이벤트 체인의 결과를 외부에서 검증**해야 할 경우, `@Commit` 또는 `TestTransaction.flagForCommit()`을 사용하고 `@AfterEach`에서 수동 정리.

---

## 핵심 파일 경로

| 파일 | 역할 |
|---|---|
| `src/main/java/.../quote/application/QuoteService.java` | submit/approve/reject 유스케이스 |
| `src/main/java/.../salesorder/application/SalesOrderService.java` | create/convertFromQuote + 이벤트 발행 |
| `src/main/java/.../shipment/application/ShipmentService.java` | on() 이벤트 리스너, complete() |
| `src/main/java/.../revenue/application/RevenueService.java` | close/cancel/delete 가드 |
| `src/main/java/.../common/util/DocumentNumberGenerator.java` | 채번 로직 (MAX 쿼리 기반) |
| `src/main/java/.../salesorder/application/SalesOrderCreatedEvent.java` | 이벤트 레코드 |
| `src/main/java/.../quote/application/QuoteConvertedToOrderEvent.java` | 이벤트 레코드 |
| `src/main/resources/application-local.yaml` | datasource 구조 참조 |

---

## 검증 방법

```bash
# 통합 테스트만 실행 (Docker 필요 — Testcontainers가 자동 기동)
./gradlew test --tests "*.application.*IntegrationTest"

# 단위 + 통합 전체 실행
./gradlew test
```

Testcontainers가 PostgreSQL 컨테이너를 자동 기동/종료하므로 로컬 DB 불필요.
