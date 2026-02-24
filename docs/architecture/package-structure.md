# 백엔드 패키지 구조 (도메인 기반)

## 결정 사항

- **도메인 우선(Domain-first)** 구조를 사용한다.
- 각 도메인은 **api / application / domain** 세 계층으로 구분한다.
- Spring Modulith와의 정합성을 유지한다.

## 루트 패키지

```
com.github.gwiman.mini_mes_backend
```

## 계층 역할

| 계층 | 패키지 | 역할 |
|------|--------|------|
| **api** | `{domain}.api` | HTTP/GraphQL 진입점 — Controller, 요청/응답 DTO |
| **application** | `{domain}.application` | 유스케이스 — Service (트랜잭션, 오케스트레이션) |
| **domain** | `{domain}.domain` | 도메인 핵심 — Entity, Repository |

## 도메인별 패키지 구조

```
com.github.gwiman.mini_mes_backend
├── MiniMesBackendApplication.java
│
├── commoncode/                    # 공통 코드
│   ├── api/
│   │   ├── CommonCodeController.java
│   │   └── dto/
│   │       ├── CommonCodeRequest.java
│   │       └── CommonCodeResponse.java
│   ├── application/
│   │   └── CommonCodeService.java
│   └── domain/
│       ├── CommonCode.java        # Entity
│       └── CommonCodeRepository.java
│
├── item/                          # 품목 정보
│   ├── api/
│   │   ├── ItemController.java
│   │   └── dto/
│   ├── application/
│   │   └── ItemService.java
│   └── domain/
│       ├── Item.java
│       └── ItemRepository.java
│
└── partner/                       # 거래처 정보
    ├── api/
    │   ├── PartnerController.java
    │   └── dto/
    ├── application/
    │   └── PartnerService.java
    └── domain/
        ├── Partner.java
        └── PartnerRepository.java
```

## 개발 대상 도메인 목록

1. **commoncode** — 공통 코드
2. **item** — 품목 정보
3. **partner** — 거래처 정보

## 공통/공유

- 여러 도메인에서 쓰는 DTO·유틸·예외는 `common` 패키지에 둔다.
- 예: `common.dto.PageResponse`, `common.exception.*`

## 참고

- `@SpringBootApplication` 은 루트 패키지에 있으므로 위 도메인 패키지들이 모두 컴포넌트 스캔 대상이다.
- 모듈 간 참조는 **application 서비스**를 통해 하며, 도메인 패키지 간 직접 의존은 최소화한다.
