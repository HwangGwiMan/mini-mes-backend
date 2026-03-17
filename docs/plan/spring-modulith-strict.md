# Spring Modulith 경계 강화 계획

> 작성일: 2026-03-17
> 최종 업데이트: 2026-03-17 (전 작업 완료)

## 현황

각 도메인 패키지에 `@ApplicationModule` + `ApplicationModulesTest.verify()`는 적용되어 있으나,
경계가 실질적으로 강제되지 않는 부분이 다수 존재한다.

---

## 문제점

### 1. 다른 모듈의 `api` 레이어 DTO를 서비스에서 직접 참조

`QuoteService`가 `employee` 모듈의 `api.dto.EmployeeResponse`를 직접 import한다.
서비스 레이어가 다른 모듈의 HTTP 응답 DTO에 의존하는 것은 레이어 위반이자 모듈 경계 위반이다.

```java
// QuoteService.java — 위반 사례
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;
```

### 2. `allowedDependencies` 미선언

모든 `package-info.java`가 `@ApplicationModule`만 선언되어 있고 허용 의존성이 명시되지 않았다.
`verify()`는 순환 참조나 `internal` 패키지 접근은 잡지만,
**의도하지 않은 모듈 간 의존이 추가되어도 감지하지 못한다.**

### 3. `QuoteConvertedToOrderEvent` 소유권 오류

이벤트가 `salesorder.application` 패키지에 선언되어 있다.
이벤트는 **발행 주체가 소유**해야 한다.
`quote` 모듈이 수주 전환을 트리거하므로 이벤트는 `quote` 모듈에 있어야 하며,
`salesorder`는 이를 구독(consume)하는 역할이다.

```
현재: salesorder.application.QuoteConvertedToOrderEvent  ← salesorder 소유 (잘못됨)
올바름: quote.application.QuoteConvertedToOrderEvent      ← quote 소유, salesorder 구독
```

### 4. 모듈별 격리 테스트 없음

`ApplicationModulesTest`는 전체 모듈 구조만 검증한다.
각 모듈을 독립적으로 로드해 경계가 실제로 지켜지는지 검증하는 `@ApplicationModuleTest`가 없다.

---

## 개선 방향

### ① `allowedDependencies` 명시 (우선순위: 높음)

각 `package-info.java`에 허용 의존 모듈을 선언한다.
선언하지 않은 모듈을 참조하면 `verify()` 시 테스트가 깨진다.

```java
// quote/package-info.java
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"partner", "employee", "item", "auth", "commoncode"}
)
package com.github.gwiman.mini_mes_backend.quote;
```

### ② 다른 모듈 `api` DTO 참조 제거 (우선순위: 높음)

`QuoteService`가 `EmployeeResponse`에서 필요한 것은 결재자 이름 하나다.
두 가지 방법 중 선택:

**방법 A** — `employee` 모듈 루트에 모듈 간 전용 타입 노출
```java
// employee 루트 패키지 (api 레이어 아님)
public record EmployeeSummary(Long id, String name) {}
```

**방법 B** — `EmployeeService`에 스칼라 반환 메서드 추가 (더 간결)
```java
// EmployeeService
public String findNameById(Long id) { ... }
```

`QuoteService`에서 필요한 건 이름 하나이므로 **방법 B** 권장.

### ③ `QuoteConvertedToOrderEvent` 이전 (우선순위: 높음)

```
이동: salesorder.application → quote.application
```

- `quote` 모듈: 수주 전환 시 이벤트 발행 (`ApplicationEventPublisher`)
- `salesorder` 모듈: `@ApplicationModuleListener`로 구독해 수주 생성 처리

### ④ 모듈별 격리 테스트 추가 (우선순위: 중간)

`STANDALONE` 모드는 해당 모듈 Bean만 로드한다.
경계 밖 의존이 있으면 컨텍스트 로드 자체가 실패하므로 경계 위반을 조기에 감지할 수 있다.

```java
@ApplicationModuleTest(mode = STANDALONE)
class QuoteModuleTest {
    // 타 모듈 Service는 자동으로 Mock 처리됨
    // 경계 위반 시 컨텍스트 로드 실패
}
```

---

## 작업 우선순위 요약

| 우선순위 | 작업 | 기대 효과 | 상태 |
|---|---|---|---|
| 높음 | `allowedDependencies` 선언 | 의도치 않은 의존 추가 즉시 감지 | ✅ 완료 |
| 높음 | `QuoteConvertedToOrderEvent` 이전 | 이벤트 발행 소유권 정상화 | ✅ 완료 |
| 높음 | `EmployeeResponse` 직접 참조 제거 | api 레이어 DTO 모듈 밖 유출 차단 | ✅ 완료 |
| 중간 | `@ApplicationModuleTest` 추가 | 모듈 격리 보장, 회귀 방지 | ✅ 완료 |

---

## 구현 내역 (2026-03-17)

### ① `allowedDependencies` 선언 — 커밋 `937aefc`

- 10개 도메인 모듈 + `common` 모듈 `package-info.java`에 `allowedDependencies` 명시
- Spring Modulith sub-package 접근 규칙에 따라 `"module::subpackage"` 형식 사용
  - 예: `"employee::application"`, `"common::domain"`, `"jooq::tables"`
- 노출이 필요한 sub-package(application, domain, exception, security, util)에 `@NamedInterface` 추가 (13개 파일)
- `ApplicationModulesTest.verify()` 통과 확인

### ② `QuoteConvertedToOrderEvent` 이전 — 커밋 `937aefc`

- `salesorder.application.QuoteConvertedToOrderEvent` → `quote.application.QuoteConvertedToOrderEvent`
- `SalesOrderService`, `QuoteEventHandler` import 경로 수정
- 이벤트 발행 주체(`quote`)가 이벤트를 소유하고, 구독 주체(`salesorder`)가 참조하는 구조로 정상화

### ③ `EmployeeResponse` 직접 참조 제거 — 커밋 `937aefc`

- `EmployeeService.findNameById(Long id): String` 추가
- `QuoteService`에서 `EmployeeResponse` 대신 `findNameById()` 호출로 변경
- `QuoteService.findHeaderById(Long id): QuoteHeaderData` 추가, `QuoteHeaderData` record 신설
- `SalesOrderService`에서 `QuoteResponse` 대신 `QuoteHeaderData` 사용
- `ShipmentService`에서 `SalesOrderRepository` 직접 의존 제거 → `SalesOrderService.getOrderWithLines()` + `SalesOrderData` record 사용

#### 부수 수정

- `auth ↔ common` 순환 해소: `CustomUserDetailsService`를 `common.security` → `auth.application`으로 이동, `SecurityConfig`는 `UserDetailsService` 인터페이스로만 주입
- `DataInitializer`를 루트 패키지로 이동 (common 모듈 외부 Bean 스캔 위반 해소)
- `jooq/tables/package-info.java`를 `src/main/generated-jooq`로 이동 (Hibernate 엔티티 스캔 오류 해소)

### ④ `@ApplicationModuleTest` 추가 — 커밋 `320a78a`

- 10개 모듈(auth, commoncode, employee, item, partner, price, process, quote, salesorder, shipment)에 `*ModuleTest.java` 추가
- `STANDALONE` 모드: 해당 모듈 Bean만 로드, 외부 모듈 의존은 자동 Mock 처리
- PostgreSQL 기동 시 `contextLoads()` 통과로 모듈 경계 위반 조기 감지
