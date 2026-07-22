# SpringMind Wallet API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** API Spring Boot canônica da carteira digital com Postgres, Security Bearer mock, Flyway, Testcontainers e contrato `/api/v1` idêntico aos fronts da trilha.

**Architecture:** Monólito modular (`auth`, `wallet`, `beneficiaries`, `transfers`, `security`, `common`). Controllers → Services → JPA Repositories. Docker Compose sobe Postgres; testes de integração usam Testcontainers.

**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring Web, Security, Data JPA, Validation, Actuator, Flyway, PostgreSQL, Testcontainers, Maven Wrapper, JUnit 5.

## Global Constraints

- Package base: `dev.springmind.wallet`
- API base path: `/api/v1`
- Erros: `{ code, message, correlationId }`
- Login: `demo@vuemind.dev` / `demo123` → token `mock-jwt-demo`
- Dinheiro em **centavos** (`long`)
- Header `Idempotency-Key` no PIX; `X-Correlation-Id` em requests/responses
- Porta `8080`
- Sem WebFlux, Kafka, JWT real, cloud deploy
- Sem rastros de IA em commits
- Spec: `docs/superpowers/specs/2026-07-22-springmind-wallet-design.md`
- Contrato: copiar de `/home/frm/Documentos/wks-poc/vuemind/docs/contracts/vuemind-wallet-openapi.yaml`
- Referência comportamental: `/home/frm/Documentos/wks-poc/vuemind-api` (mesmos códigos de erro e seed)

---

## File Structure (mapa)

```
springmind/
  docker-compose.yml
  pom.xml
  mvnw, mvnw.cmd, .mvn/
  .gitignore
  README.md
  docs/contracts/vuemind-wallet-openapi.yaml
  docs/guides/entrevista-spring.md
  docs/superpowers/specs/...
  docs/superpowers/plans/...
  src/main/java/dev/springmind/wallet/
    SpringmindApplication.java
    common/ApiError.java, ApiException.java, GlobalExceptionHandler.java, CorrelationIdFilter.java
    security/SecurityConfig.java, MockBearerTokenFilter.java, RestAuthEntryPoint.java
    auth/...
    wallet/...
    beneficiaries/...
    transfers/...
  src/main/resources/
    application.yml
    db/migration/V1__init.sql
    db/migration/V2__seed.sql
  src/test/java/dev/springmind/wallet/
    PixServiceTest.java
    PixTransferIT.java
    Support/...
```

---

### Task 1: Scaffold Maven + Docker Compose + OpenAPI

**Files:**
- Create: `pom.xml`, `.gitignore`, `docker-compose.yml`, `mvnw*`, `src/main/resources/application.yml`, `src/main/java/.../SpringmindApplication.java`
- Create: `docs/contracts/vuemind-wallet-openapi.yaml` (cópia)

**Produces:** app sobe (falha sem Postgres ok até Task 2); `docker compose up -d` sobe Postgres `springmind` / user `springmind` / pass `springmind` porta 5432

- [ ] **Step 1:** Gerar projeto Spring Boot 3.3.4 Java 21 com deps: web, security, data-jpa, validation, actuator, flyway, postgresql, test, testcontainers (jdbc + postgresql + junit-jupiter)
- [ ] **Step 2:** `docker-compose.yml` com image `postgres:16-alpine`, volume, healthcheck
- [ ] **Step 3:** `application.yml` — datasource, jpa `ddl-auto: validate`, flyway enabled, server.port 8080
- [ ] **Step 4:** Copiar OpenAPI; `.gitignore` Maven/IDE
- [ ] **Step 5:** Commit `chore: scaffold Spring Boot 3.3 com Postgres e Docker Compose`

---

### Task 2: Flyway schema + entidades JPA + seed

**Files:**
- Create: `V1__init.sql`, `V2__seed.sql`
- Create: entities `UserEntity`, `AccountEntity`, `BeneficiaryEntity`, `TransactionEntity`, `TransferEntity`, `IdempotencyKeyEntity`
- Create: repositories JPA

**Schema mínimo:**
- `users(id, name, email, password_hash)` — password texto demo ok (`demo123`)
- `accounts(id, user_id, available_cents, currency)`
- `beneficiaries(id, name, pix_key)`
- `transactions(id, type, amount_cents, description, created_at, counterparty)`
- `transfers(id, beneficiary_id, amount_cents, status, created_at)`
- `idempotency_keys(key_value PK, transfer_id, created_at)`

**Seed:** user demo, account 250000, b1/b2, t1 PIX_IN 50000 Carlos

- [ ] **Step 1:** Migrations + entities + repos
- [ ] **Step 2:** Subir compose + app; verificar tabelas
- [ ] **Step 3:** Commit `feat: schema Flyway, entidades JPA e seed da carteira`

---

### Task 3: Common + Security + Auth

**Files:**
- `common/*`, `security/*`, `auth/*`

**Behavior (igual vuemind-api):**
- Login público; demais `/api/v1/**` autenticadas
- Bearer exatamente `mock-jwt-demo`
- `ApiException` + handler → `ApiError`
- Correlation filter

- [ ] **Step 1:** Implementar common + security + auth
- [ ] **Step 2:** Teste MockMvc login 200/401
- [ ] **Step 3:** Commit `feat: Security Bearer mock e endpoint de login`

---

### Task 4: Wallet + Beneficiaries

**Endpoints:**
- `GET /wallet/balance`, `GET /wallet/transactions?type=`
- `GET/POST /beneficiaries`, `DELETE /beneficiaries/{id}`

- [ ] **Step 1:** Services + controllers
- [ ] **Step 2:** Testes MockMvc básicos (balance seed, list beneficiaries)
- [ ] **Step 3:** Commit `feat: saldo, extrato e CRUD de favorecidos`

---

### Task 5: PIX + Testcontainers

**PixService.executePix** (mesma ordem do vuemind-api): idempotency → beneficiary → amount → funds → debit → transfer + PIX_OUT + store key.

- [ ] **Step 1:** TDD unitário `PixServiceTest` (INSUFFICIENT_FUNDS, idempotência) com repos mock ou `@DataJpaTest`+Testcontainers
- [ ] **Step 2:** `TransfersController` POST 201
- [ ] **Step 3:** `PixTransferIT` com Testcontainers: login → PIX 1000 → balance 249000
- [ ] **Step 4:** Commit `feat: PIX com idempotência e teste Testcontainers`

---

### Task 6: README + guia + verificação final

- [ ] **Step 1:** README (compose, mvnw, credenciais, proxy fronts, bullets entrevista)
- [ ] **Step 2:** `docs/guides/entrevista-spring.md`
- [ ] **Step 3:** `./mvnw test` + smoke run
- [ ] **Step 4:** Commit `docs: README e guia de entrevista SpringMind`

---

## Self-Review (plano × spec)

| Spec | Task |
|------|------|
| Postgres + Compose + Flyway | 1–2 |
| Auth mock + Security | 3 |
| Wallet + beneficiaries | 4 |
| PIX + idempotency + Testcontainers | 5 |
| README + guia | 6 |
| Sem Kafka/WebFlux/JWT real | Global Constraints |
