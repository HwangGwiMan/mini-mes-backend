# OpenAPI (Swagger) 도입 계획

> 작성일: 2026-03-17

## 개요

현재 12개 컨트롤러가 존재하며 도메인이 계속 추가되는 시점이다.
JWT 인증이 있어 Postman/curl 설정이 번거롭고, 프론트엔드 협업 시 API 명세가 없다.
`springdoc-openapi`를 도입하여 코드에서 문서를 자동 생성하고 Swagger UI로 직접 테스트할 수 있게 한다.

---

## 라이브러리 선택

| 후보 | 결론 |
|---|---|
| `springdoc-openapi` | **선택** — Spring Boot 3/4 공식 지원, 활발한 유지보수 |
| `springfox` | 제외 — Spring Boot 3 미지원, 사실상 deprecated |

Spring Boot 4.0.3 기준 `springdoc-openapi 2.x` 사용.

---

## 의존성 추가

```gradle
// build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6'
```

---

## 설정

### application-local.yaml

```yaml
springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    # JWT 입력 후 모든 API에 자동 적용
    persist-authorization: true
  api-docs:
    enabled: true
    path: /v3/api-docs
  # Spring Security permitAll 경로와 맞춤
  paths-to-match: /api/**
```

### application-prod.yaml

```yaml
# 운영 환경에서는 완전 비활성화
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

---

## SecurityConfig 수정

Swagger UI와 API docs 경로를 인증 없이 접근 가능하도록 허용한다.
`local` 프로필에서만 의미 있고, `prod`에서는 비활성화되므로 보안 위험 없음.

```java
// SecurityConfig.java — authorizeHttpRequests 수정
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**"
    ).permitAll()
    .anyRequest().authenticated()
)
```

---

## OpenAPI 전역 설정 클래스

`common` 패키지에 전역 설정 Bean을 추가한다.

```java
// common/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    /**
     * Swagger UI에서 JWT Bearer 토큰 입력 한 번으로 전체 API 인증 가능하게 설정.
     * /api/auth/login 으로 토큰 발급 후 Authorize 버튼에 입력하면 된다.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Mini MES API")
                .description("경량 MES(제조실행시스템) REST API")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

---

## 어노테이션 사용 원칙

자동 추론에 최대한 의존하고, 비직관적인 곳에만 보완한다.
현재 CLAUDE.md 주석 철학("왜를 설명, 반복 금지")과 동일하게 적용.

| 상황 | 처리 방법 |
|---|---|
| 메서드명/경로/HTTP method로 의미가 명확한 API | 어노테이션 불필요 |
| 비직관적인 동작 또는 제약이 있는 API | `@Operation(summary = "...")` 추가 |
| DTO 필드명이 명확한 경우 | `@Schema` 불필요 |
| 허용값이 제한되거나 형식 제약이 있는 필드 | `@Schema(description = "...", example = "...")` 추가 |
| 상태 코드가 기본값(200)과 다른 경우 | springdoc이 `@ResponseStatus` 자동 인식 — 어노테이션 불필요 |

### 적용 예시

```java
// 명확한 경우 — 어노테이션 없이도 충분
@GetMapping("/{id}")
public QuoteResponse getById(@PathVariable Long id) { ... }

// 비직관적인 경우 — 상태 전이 규칙을 명시
@Operation(summary = "견적 제출", description = "작성(DRAFT) 상태 견적만 제출 가능. 담당자 또는 관리자만 허용.")
@PatchMapping("/{id}/submit")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void submit(@PathVariable Long id, Authentication authentication) { ... }
```

---

## 태그(그룹) 구성

컨트롤러에 `@Tag`를 붙여 Swagger UI에서 도메인별로 그룹핑한다.

```java
@Tag(name = "견적 (Quote)")
@RestController
@RequestMapping("/api/quotes")
public class QuoteController { ... }
```

| 컨트롤러 | 태그명 |
|---|---|
| AuthController | 인증 (Auth) |
| CodeGroupController | 코드그룹 (CodeGroup) |
| CommonCodeController | 공통코드 (CommonCode) |
| EmployeeController | 담당자 (Employee) |
| ItemController | 품목 (Item) |
| ItemPriceController | 품목단가 (ItemPrice) |
| PartnerController | 거래처 (Partner) |
| ProcessController | 공정 (Process) |
| QuoteController | 견적 (Quote) |
| SalesOrderController | 수주 (SalesOrder) |
| ShipmentController | 출하 (Shipment) |

> **제외 대상**: `AuthGraphqlController` — GraphQL 엔드포인트(`/graphql`)는 REST OpenAPI 문서 대상이 아니므로 `@Tag` 적용 제외.

---

## 구현 순서

### Step 1. 의존성 및 설정
- [ ] `build.gradle`에 `springdoc-openapi-starter-webmvc-ui` 추가
- [ ] `application-local.yaml`에 springdoc 설정 추가
- [ ] `application-prod.yaml`에 비활성화 설정 추가

### Step 2. 공통 설정
- [ ] `common/config/OpenApiConfig.java` 생성 — JWT Bearer 인증 스킴 등록
- [ ] `SecurityConfig.java` — Swagger 경로 `permitAll` 추가

### Step 3. 컨트롤러 태그 적용
- [ ] 전체 컨트롤러(11개)에 `@Tag` 추가

### Step 4. 선택적 어노테이션 보완

#### 상태 전이 API — `@Operation` 추가 대상

| 컨트롤러 | 엔드포인트 | 전이 규칙 |
|---|---|---|
| QuoteController | `PATCH /{id}/submit` | DRAFT → SUBMITTED. 담당자 또는 관리자만 허용 |
| QuoteController | `PATCH /{id}/approve` | SUBMITTED → APPROVED. 관리자만 허용 |
| QuoteController | `PATCH /{id}/reject` | SUBMITTED → REJECTED. 관리자만 허용 |
| QuoteController | `PATCH /{id}/convert` | APPROVED 상태 견적만 수주 전환 가능 |
| ShipmentController | `POST /{id}/complete` | 출하대기(SHIPMENT_STATUS_01) 상태에서만 출하완료(SHIPMENT_STATUS_03)로 전환 가능 |

- [ ] 위 5개 엔드포인트에 `@Operation(summary = "...", description = "...")` 추가

#### 제한된 DTO 필드 — `@Schema(example = "...")` 추가 대상

- [ ] 상태 코드 필드 등 허용값이 제한된 DTO 필드 — `@Schema(example = "...")` 추가

---

## 검증

1. `./gradlew bootRun` 실행 후 `http://localhost:8080/swagger-ui.html` 접근
2. `/api/auth/login`으로 토큰 발급 → Swagger UI `Authorize`에 입력
3. 각 도메인 API 직접 호출 테스트
4. `http://localhost:8080/v3/api-docs`에서 JSON 스펙 확인
