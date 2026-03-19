# Layer 3: 컨트롤러 슬라이스 테스트 개발 계획

## Context

`@WebMvcTest`로 웹 레이어만 로드하여 서비스 레이어는 Mockito로 대체한다. DB와 전체 Spring Context 없이 **빠르게** 다음을 검증한다:

- HTTP 상태 코드 (201, 204, 400, 401, 404, 409)
- Bean Validation 거부 응답 (400 + 필드 오류 메시지)
- 인증 없는 접근 거부 (401)
- 응답 JSON 구조 (핵심 필드 존재 여부)

---

## 의존성 추가 불필요

`build.gradle`에 이미 포함:
```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-security-test'  // @WithMockUser
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'    // @WebMvcTest, MockMvc
```

---

## Security 처리 전략

SecurityConfig는 모든 `/api/**`에 인증을 요구하고 `JwtAuthenticationFilter`를 필터 체인에 등록한다. 슬라이스 테스트에서는 두 가지 방식으로 처리한다:

| 상황 | 방식 |
|---|---|
| 인증된 요청 테스트 | `@WithMockUser` — SecurityContext에 직접 MockUser 주입, JWT 필터 우회 |
| 인증 없는 401 테스트 | `@MockitoBean JwtAuthenticationFilter` — 필터가 아무 작업도 안 해 인증 없이 통과, Security가 401 반환 |
| 권한별 테스트 (submit ADMIN) | `@WithMockUser(roles = "ADMIN")` |

`JwtTokenProvider`와 `JwtAuthenticationFilter`를 `@MockitoBean`으로 등록하면 실제 JWT 검증 로직 없이 Security 설정만 동작한다.

---

## 글로벌 예외 처리 매핑 (GlobalExceptionHandler)

| 예외 | HTTP 상태 |
|---|---|
| `MethodArgumentNotValidException` | 400 |
| `ResourceNotFoundException` | 404 |
| `BusinessRuleViolationException` | 409 |
| `IllegalArgumentException` | 400 |
| 그 외 `Exception` | 500 |

---

## 테스트 파일 구조

```
src/test/java/com/github/gwiman/mini_mes_backend/
├── quote/api/
│   └── QuoteControllerTest.java
├── salesorder/api/
│   └── SalesOrderControllerTest.java
├── shipment/api/
│   └── ShipmentControllerTest.java
└── revenue/api/
    └── RevenueControllerTest.java
```

---

## 기반 설정 패턴

각 테스트 클래스에 공통 적용:

```java
@WebMvcTest(QuoteController.class)
@Import(SecurityConfig.class)   // Security 필터 체인 포함
class QuoteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean QuoteService quoteService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean UserDetailsService userDetailsService;
    // ...
}
```

---

## 테스트 케이스 명세

### 1. `QuoteControllerTest` — `/api/quotes`

#### 공통 픽스처
```java
private QuoteRequest validRequest() {
    return new QuoteRequest(LocalDate.now(), null, 1L, 1L, 2L, null, null,
        List.of(new QuoteLineRequest(1L, new BigDecimal("10"), new BigDecimal("1000"),
                null, null, 0)));
}
```

| 테스트 메서드 | HTTP | 검증 내용 |
|---|---|---|
| `create_유효한요청_201` | POST `/api/quotes` | `@WithMockUser`, 201, 응답에 `quoteNumber` 필드 존재 |
| `create_partnerId_null_400` | POST | `@WithMockUser`, 400, `errors.partnerId` 존재 |
| `create_quoteDate_null_400` | POST | `@WithMockUser`, 400, `errors.quoteDate` 존재 |
| `create_lines_empty_400` | POST | `@WithMockUser`, 400, `errors.lines` 존재 |
| `create_line_quantity_zero_400` | POST | `@WithMockUser`, quantity=0 → 400 |
| `create_미인증_401` | POST | 인증 없음 → 401 |
| `getById_존재하지않는_404` | GET `/api/quotes/999` | `@WithMockUser`, service가 `ResourceNotFoundException` throw → 404 |
| `submit_admin권한_204` | PATCH `/api/quotes/1/submit` | `@WithMockUser(roles="ADMIN")`, 204 |
| `submit_service_BusinessRule_409` | PATCH | `@WithMockUser`, service가 `BusinessRuleViolationException` throw → 409 |
| `approve_유효한요청_204` | POST `/api/quotes/1/approve` | `@WithMockUser`, 204 |
| `approve_comment_500자초과_400` | POST | `@WithMockUser`, comment 501자 → 400 |
| `delete_존재하지않는_404` | DELETE | `@WithMockUser`, service가 `ResourceNotFoundException` → 404 |

---

### 2. `SalesOrderControllerTest` — `/api/sales-orders`

| 테스트 메서드 | HTTP | 검증 내용 |
|---|---|---|
| `create_유효한요청_201` | POST `/api/sales-orders` | `@WithMockUser`, 201, `orderNumber` 필드 존재 |
| `create_orderDate_null_400` | POST | 400, `errors.orderDate` |
| `create_partnerId_null_400` | POST | 400, `errors.partnerId` |
| `create_lines_empty_400` | POST | 400, `errors.lines` |
| `create_미인증_401` | POST | 401 |
| `getById_404` | GET `/{id}` | service → `ResourceNotFoundException` → 404 |
| `convertFromQuote_미승인_409` | POST `/from-quote/1` | service → `BusinessRuleViolationException` → 409 |
| `convertFromQuote_미인증_401` | POST | 401 |
| `delete_204` | DELETE `/{id}` | `@WithMockUser`, 204 |

---

### 3. `ShipmentControllerTest` — `/api/shipments`

> 출하 생성 POST 엔드포인트 없음 (수주 생성 이벤트로 자동 생성).

| 테스트 메서드 | HTTP | 검증 내용 |
|---|---|---|
| `getAll_200` | GET `/api/shipments` | `@WithMockUser`, 200, 빈 배열 |
| `getById_404` | GET `/{id}` | service → `ResourceNotFoundException` → 404 |
| `update_유효한요청_200` | PUT `/{id}` | `@WithMockUser`, 200 |
| `update_statusCode_null_400` | PUT | 400, `errors.statusCode` |
| `update_lines_empty_400` | PUT | 400, `errors.lines` |
| `update_완료된출하_409` | PUT | service → `BusinessRuleViolationException` → 409 |
| `complete_유효한요청_200` | POST `/{id}/complete` | `@WithMockUser`, 200, 응답에 `shipmentDate` 존재 |
| `complete_shipmentDate_null_400` | POST | 400, `errors.shipmentDate` |
| `complete_lines_empty_400` | POST | 400, `errors.lines` |
| `delete_대기외상태_409` | DELETE | service → `BusinessRuleViolationException` → 409 |
| `delete_미인증_401` | DELETE | 401 |

---

### 4. `RevenueControllerTest` — `/api/revenues`

| 테스트 메서드 | HTTP | 검증 내용 |
|---|---|---|
| `create_유효한요청_200` | POST `/api/revenues` | `@WithMockUser`, 200 (`@ResponseStatus` 없으므로 200) |
| `create_partnerId_null_400` | POST | 400 |
| `create_revenueDate_null_400` | POST | 400 |
| `create_lines_empty_400` | POST | 400 |
| `create_미인증_401` | POST | 401 |
| `getById_404` | GET `/{id}` | service → `ResourceNotFoundException` → 404 |
| `close_200` | POST `/{id}/close` | `@WithMockUser`, 200 |
| `close_마감후다시마감_409` | POST | service → `BusinessRuleViolationException` → 409 |
| `cancel_200` | POST `/{id}/cancel` | `@WithMockUser`, 200 |
| `update_마감후수정_409` | PUT | service → `BusinessRuleViolationException` → 409 |
| `delete_마감후삭제_409` | DELETE | service → `BusinessRuleViolationException` → 409 |
| `findAvailableOrderLines_200` | GET `/available-lines?partnerId=1` | `@WithMockUser`, 200 |

---

## MockMvc 사용 패턴

### 성공 케이스 예시
```java
@Test
@WithMockUser
void create_유효한요청_201() throws Exception {
    given(quoteService.create(any())).willReturn(mockResponse());

    mockMvc.perform(post("/api/quotes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.quoteNumber").exists());
}
```

### 예외 케이스 예시 (409)
```java
@Test
@WithMockUser
void submit_service_BusinessRule_409() throws Exception {
    willThrow(new BusinessRuleViolationException("이미 제출된 견적입니다."))
        .given(quoteService).submit(anyLong(), anyString(), anyBoolean());

    mockMvc.perform(patch("/api/quotes/1/submit"))
        .andExpect(status().isConflict());
}
```

### Bean Validation 케이스 예시 (400)
```java
@Test
@WithMockUser
void create_partnerId_null_400() throws Exception {
    QuoteRequest invalid = new QuoteRequest(LocalDate.now(), null, null /* partnerId */,
        1L, 2L, null, null, List.of(...));

    mockMvc.perform(post("/api/quotes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.partnerId").exists());
}
```

---

## 핵심 파일 경로

| 파일 | 역할 |
|---|---|
| `src/main/java/.../quote/api/QuoteController.java` | `Authentication` 주입, submit/approve/reject |
| `src/main/java/.../salesorder/api/SalesOrderController.java` | `convertFromQuote` 경로 |
| `src/main/java/.../shipment/api/ShipmentController.java` | POST 없음 (자동생성), complete |
| `src/main/java/.../revenue/api/RevenueController.java` | close/cancel, `available-lines` 경로 우선순위 |
| `src/main/java/.../common/exception/GlobalExceptionHandler.java` | 예외 → HTTP 상태 매핑 기준 |
| `src/main/java/.../common/security/SecurityConfig.java` | `@Import` 대상, 인증 필터 체인 |
| `src/main/java/.../quote/api/dto/QuoteRequest.java` | Bean Validation 기준 |
| `src/main/java/.../salesorder/api/dto/SalesOrderRequest.java` | Bean Validation 기준 |
| `src/main/java/.../shipment/api/dto/ShipmentCompleteRequest.java` | Bean Validation 기준 |
| `src/main/java/.../revenue/api/dto/RevenueCreateRequest.java` | Bean Validation 기준 |

---

## 검증 방법

```bash
# 컨트롤러 슬라이스 테스트만 실행 (DB 불필요, 매우 빠름)
./gradlew test --tests "*.api.*ControllerTest"

# 전체 테스트 (Layer 1 + 2 + 3)
./gradlew test
```
