# Flyway 마이그레이션 도입 계획

## Context

현재 프로젝트는 스키마 관리를 JPA `ddl-auto: update`에 의존하고 있어 아래 문제가 있다:
- 컬럼 삭제 / 타입 변경이 자동 반영되지 않아 prod 배포 시 위험
- jOOQ codegen이 실제 DB를 읽기 때문에 팀원 간 DB 상태 불일치가 jOOQ 코드 불일치로 이어짐
- `revenue`, `shipment`, `item_price` 테이블은 엔티티는 있으나 DB에 아직 없음 (jOOQ에 미등록)
- 스키마 변경 이력이 코드로 남지 않음

Flyway 도입으로 스키마 버전 관리를 코드화하고, `ddl-auto`를 `validate/none`으로 전환한다.

---

## 작업 단계

### Step 1 — 의존성 추가 (build.gradle)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-flyway'
```

### Step 2 — Flyway 디렉터리 생성

```
src/main/resources/db/migration/
```

Spring Boot 기본 경로이므로 별도 설정 불필요.

### Step 3 — V1__init.sql 작성 (현재 스키마 전체)

현재 DB에 존재하는 13개 테이블을 `pg_dump --schema-only`로 덤프한 뒤 정리.
대상 테이블 (jOOQ Tables.java 기준):
- `code_group`, `common_code`
- `employee`
- `event_publication` (Spring Modulith 관리 테이블)
- `item`
- `partner`
- `process`
- `quote`, `quote_approval`, `quote_line`
- `sales_order`, `sales_order_line`
- `users`

> **주의**: `event_publication`은 Spring Modulith가 관리하지만 Flyway로 함께 버전 관리하는 것이 안전.
> 또는 `flyway.table-exclusions`로 제외 가능 (Step 4에서 결정).

파일: `src/main/resources/db/migration/V1__init.sql`

### Step 4 — V2__add_missing_domains.sql 작성

현재 DB에 없는 3개 도메인 테이블 DDL 작성:

**item_price**
```sql
CREATE TABLE item_price (
    id          BIGSERIAL PRIMARY KEY,
    item_id     BIGINT NOT NULL UNIQUE,
    unit_price  NUMERIC(19,2) NOT NULL,
    remarks     VARCHAR(200),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);
```

**shipment / shipment_line**
```sql
CREATE TABLE shipment (
    id               BIGSERIAL PRIMARY KEY,
    shipment_number  VARCHAR(50) NOT NULL UNIQUE,
    sales_order_id   BIGINT NOT NULL,
    shipment_date    DATE,
    partner_id       BIGINT NOT NULL,
    employee_id      BIGINT,
    status_code      VARCHAR(20) NOT NULL,
    remarks          VARCHAR(200),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE TABLE shipment_line (
    id                   BIGSERIAL PRIMARY KEY,
    shipment_id          BIGINT NOT NULL,
    sales_order_line_id  BIGINT NOT NULL,
    item_id              BIGINT NOT NULL,
    planned_quantity     NUMERIC(19,4) NOT NULL,
    actual_quantity      NUMERIC(19,4),
    unit_price           NUMERIC(19,4) NOT NULL,
    planned_amount       NUMERIC(19,4) NOT NULL,
    actual_amount        NUMERIC(19,4),
    remarks              VARCHAR(200),
    sort_order           INT,
    created_at           TIMESTAMP NOT NULL,
    updated_at           TIMESTAMP NOT NULL
);
```

**revenue / revenue_line**
```sql
CREATE TABLE revenue (
    id              BIGSERIAL PRIMARY KEY,
    revenue_number  VARCHAR(50) NOT NULL UNIQUE,
    partner_id      BIGINT NOT NULL,
    employee_id     BIGINT,
    revenue_date    DATE NOT NULL,
    status_code     VARCHAR(20) NOT NULL,
    remarks         VARCHAR(200),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE TABLE revenue_line (
    id                   BIGSERIAL PRIMARY KEY,
    revenue_id           BIGINT NOT NULL,
    sales_order_line_id  BIGINT NOT NULL,
    sales_order_id       BIGINT NOT NULL,
    item_id              BIGINT NOT NULL,
    quantity             NUMERIC(19,4) NOT NULL,
    unit_price           NUMERIC(19,4) NOT NULL,
    amount               NUMERIC(19,4) NOT NULL,
    remarks              VARCHAR(200),
    sort_order           INT,
    created_at           TIMESTAMP NOT NULL,
    updated_at           TIMESTAMP NOT NULL
);
```

파일: `src/main/resources/db/migration/V2__add_missing_domains.sql`

### Step 5 — Flyway 설정 (application-local.yaml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true   # 기존 DB에 flyway_schema_history 없을 때 자동 baseline
    baseline-version: 0
  jpa:
    hibernate:
      ddl-auto: validate        # update → validate 로 변경
```

### Step 6 — Flyway 설정 (application-prod.yaml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    # baseline-on-migrate: false (prod는 명시적 baseline 적용 후 false 유지)
  jpa:
    hibernate:
      ddl-auto: none            # 이미 validate였으나 none으로 강화
```

### Step 7 — Gradle task 체인 정비 (build.gradle)

현재: `jooqCodegen → compileJava`
목표: `flywayMigrate → jooqCodegen → compileJava`

단, DB 없는 환경(`skipJooqCodegen`)에서는 Flyway도 스킵:

```groovy
// 기존 로직 유지, flywayMigrate 연결만 추가
if (!project.hasProperty('skipJooqCodegen')) {
    tasks.named('jooqCodegen') {
        dependsOn(tasks.named('flywayMigrate'))
    }
    tasks.named('compileJava') {
        dependsOn(tasks.named('jooqCodegen'))
    }
}
```

> Flyway Gradle 플러그인(`org.flywaydb.flyway`)을 추가해야 `flywayMigrate` task 사용 가능.
> 단, Spring Boot starter만 써도 `bootRun` 시 자동 migrate는 동작함.
> Gradle task 체인이 필요한 경우에만 플러그인 추가 — 아닌 경우 starter만으로 충분.

### Step 8 — jOOQ codegen 재실행 및 검증

```bash
# 로컬 PostgreSQL 기동 후
./gradlew jooqCodegen
# → revenue, shipment, item_price 테이블이 jOOQ에 추가되어야 함
./gradlew build -PskipJooqCodegen
./gradlew test
```

---

## 수정 대상 파일

| 파일 | 변경 내용 |
|------|-----------|
| `build.gradle` | flyway-gradle 플러그인 추가 (선택), task 체인 정비 |
| `src/main/resources/application-local.yaml` | flyway 설정 추가, ddl-auto: validate |
| `src/main/resources/application-prod.yaml` | flyway 설정 추가, ddl-auto: none |
| `src/main/resources/db/migration/V1__init.sql` | 신규 생성 — 기존 13개 테이블 DDL |
| `src/main/resources/db/migration/V2__add_missing_domains.sql` | 신규 생성 — item_price, shipment, shipment_line, revenue, revenue_line |

---

## 확정 사항

| 항목 | 결정 |
|------|------|
| V1 SQL 작성 방식 | `pg_dump --schema-only` 덤프 후 Claude가 Flyway용으로 정리 |
| event_publication | Flyway에서 **제외** — Spring Modulith 자체 초기화에 위임 |
| Gradle task 체인 | **자동 체인** 구성 — Flyway Gradle 플러그인 추가 필요 |

---

## 검증 방법

```bash
# 1. 로컬 DB 초기화 후 재생성 테스트
dropdb mini_mes && createdb mini_mes
./gradlew bootRun
# → flyway가 V1, V2 순서로 migrate 실행 로그 확인

# 2. jOOQ 재생성
./gradlew jooqCodegen
# → generated-jooq에 revenue, shipment, item_price 클래스 생성 확인

# 3. 빌드 및 테스트
./gradlew build -PskipJooqCodegen
./gradlew test
```
