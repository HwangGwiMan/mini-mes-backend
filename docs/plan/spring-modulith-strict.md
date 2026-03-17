# Spring Modulith 경계 강화 계획

> 작성일: 2026-03-17

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

| 우선순위 | 작업 | 기대 효과 |
|---|---|---|
| 높음 | `allowedDependencies` 선언 | 의도치 않은 의존 추가 즉시 감지 |
| 높음 | `QuoteConvertedToOrderEvent` 이전 | 이벤트 발행 소유권 정상화 |
| 높음 | `EmployeeResponse` 직접 참조 제거 | api 레이어 DTO 모듈 밖 유출 차단 |
| 중간 | `@ApplicationModuleTest` 추가 | 모듈 격리 보장, 회귀 방지 |
