# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mini-MES: a lightweight Manufacturing Execution System backend built with Spring Boot 4, Java 17, and PostgreSQL.

## Commands

```bash
# Build (skips jOOQ codegen if DB is unavailable)
./gradlew build -PskipJooqCodegen

# Build with jOOQ codegen (requires local PostgreSQL running)
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.github.gwiman.mini_mes_backend.SomeTest"

# Regenerate jOOQ classes (requires PostgreSQL at localhost:5432/mini_mes)
./gradlew jooqCodegen

# Run the application (uses application-local.yaml by default)
./gradlew bootRun
```

**Database**: PostgreSQL at `localhost:5432/mini_mes`, user/password: `postgres/postgres` (local profile).

## Architecture

### Package Structure (Domain-first)

Each domain lives under `com.github.gwiman.mini_mes_backend.{domain}` and is split into three layers:

| Layer | Package | Responsibility |
|---|---|---|
| `api` | Controllers + DTOs | HTTP entry point, request/response mapping |
| `application` | Services | Transactions, use-case orchestration |
| `domain` | Entities + Repositories | JPA entities and Spring Data repositories |
| `internal` | QueryRepositories | Complex read queries using jOOQ (module-private) |

### Domains

- **commoncode** — Code groups and common codes (lookup values)
- **employee** — Employees (담당자)
- **item** — Items/products (품목)
- **partner** — Business partners / customers (거래처)
- **process** — Manufacturing processes (공정)
- **quote** — Sales quotes (견적); has header + line items
- **salesorder** — Sales orders (수주); converted from quotes, has header + line items

### Key Design Patterns

- **Write path**: JPA (`Repository.save()`) — entities use protected no-arg constructors (Lombok `@NoArgsConstructor(access = PROTECTED)`) and explicit public constructors / update methods.
- **Read path**: jOOQ (`*QueryRepository`) — complex joins and filters use `DSLContext`. jOOQ-generated classes live in `src/main/generated-jooq` under package `com.github.gwiman.mini_mes_backend.jooq`.
- **Cross-domain access**: Services call other domains' public `Service` APIs only (e.g., `partnerService.exists()`, `quoteService.getLines()`). Direct cross-domain `Repository` injection is prohibited. Domain entities store foreign keys as `Long` IDs, not `@ManyToOne` references.
- **Module events**: Cross-domain state changes use Spring Modulith `@ApplicationModuleListener` (e.g., `QuoteConvertedToOrderEvent`).
- **Document numbering**: Auto-generated in service (e.g., `SO_YYYYMM_001`) using jOOQ max-query on the number column.
- **Status codes**: Stored as `String` columns referencing `commoncode` values (e.g., `QUOTE_STATUS_05`).

### Security

JWT-based authentication (`JwtTokenProvider`, `JwtAuthenticationFilter`). Spring Security config in `common/security/SecurityConfig.java`. Auth endpoints under `/api/auth/**`.

### Profiles

- `local` — default, uses `application-local.yaml`
- `prod` — uses `application-prod.yaml`, set via `SPRING_PROFILES_ACTIVE=prod`

## Comment Style

- Write in **Korean**.
- Explain **why**, not what — don't repeat what the code already says.
- Prioritize business rules: numbering schemes, status transitions, common-code linkage, etc.
- Keep comments in sync with code when making changes.

| Location | Format | When to write |
|---|---|---|
| `public` class | Javadoc `/** */` | Always — state domain role and layer |
| `public` method | Javadoc | When logic is non-trivial or has constraints; include `@param`/`@return`/`@throws` only when non-obvious |
| `private` method | Inline or omit | Omit if the name is self-explanatory |
| Inline | `//` | Business rules, non-intuitive decisions |
