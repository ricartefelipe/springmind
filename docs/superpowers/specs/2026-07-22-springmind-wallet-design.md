# SpringMind Wallet API — Design Spec (Spring Boot 3.3)

**Data:** 2026-07-22  
**Status:** aprovado para plano de implementação  
**Repositório:** `springmind`  
**Produto:** API REST da carteira digital de estudo (auth, saldo, extrato, PIX, favorecidos)  
**Papel na trilha:** backend canônico com Postgres (o `vuemind-api` permanece como skeleton in-memory + demo WebFlux)

---

## 1. Contexto e objetivo

Parte da trilha de preparação técnica iniciada em `vuemind`. Os fronts (Vue/React/Angular) já falam o contrato OpenAPI `/api/v1` via MSW. O SpringMind implementa esse contrato de verdade, com banco, segurança, Docker e testes de integração — cobrindo o que as respostas à recrutadora citaram em Spring Boot, Security, Data JPA, containers e Testcontainers.

### Objetivos didáticos do SpringMind v1

1. API Spring Boot 3.3 + Java 21 alinhada ao OpenAPI da trilha.
2. Persistência com Spring Data JPA + PostgreSQL + Flyway.
3. Spring Security com Bearer opaco mock (compatível com fronts/MSW).
4. Docker Compose para subir o Postgres; Testcontainers no teste do PIX.
5. Pitch de entrevista curto (README + guia): correlation id, idempotência, centavos, camadas por feature.

### Fora de escopo (v1)

- Kafka / RabbitMQ / outbox (citar na entrevista; implementar em v2 se sobrar tempo)
- WebFlux (já demonstrado no `vuemind-api`)
- JWT real assinado
- Deploy cloud (AWS/Azure/GCP)
- Cobertura JaCoCo alta / review loops elaborados
- Mobile (Ionic / RN / Flutter)

---

## 2. Decisões travadas

| Tema | Decisão |
|------|---------|
| Domínio | Carteira digital (mesmo dos fronts) |
| Relação com `vuemind-api` | SpringMind é o backend canônico; `vuemind-api` fica referência rápida |
| Escopo | Meio-termo: auth + saldo + extrato + PIX + CRUD favorecidos |
| Contrato | Cópia de `vuemind-wallet-openapi.yaml` em `docs/contracts/` |
| Stack | Java 21, Spring Boot 3.3 (Web, Security, Data JPA, Validation, Actuator) |
| Banco | PostgreSQL via Docker Compose; Flyway |
| Auth | Token opaco `mock-jwt-demo`; login `demo@vuemind.dev` / `demo123` |
| Dinheiro | Centavos (`long` / integer no JSON) |
| PIX | Header `Idempotency-Key` no POST; 409 `INSUFFICIENT_FUNDS` |
| Erros | `{ code, message, correlationId }` |
| Correlation | Header `X-Correlation-Id` (gerar se ausente) |
| Arquitetura | Monólito modular por feature (Controller → Service → Repository) |
| Testes | Domínio puro onde couber + `@SpringBootTest` do PIX com Testcontainers |
| Build | Maven Wrapper (`./mvnw`) |

---

## 3. Personas e fluxos

**Cliente:** qualquer front da trilha (ou `curl`) autenticado.

### Fluxos

1. **Login** — credenciais demo → `{ accessToken, user }`.
2. **Saldo** — `GET /wallet/balance` com Bearer.
3. **Extrato** — `GET /wallet/transactions` (filtro opcional `type`).
4. **Favorecidos** — listar / criar / remover.
5. **PIX** — `POST /transfers/pix` com `Idempotency-Key` → comprovante; saldo e extrato atualizam.

---

## 4. Arquitetura

### 4.1 Visão geral

```
Fronts (Vue / React / Angular)
        │  /api/v1 + Bearer + X-Correlation-Id
        ▼
SpringMind (Boot 3.3)
  security/     → MockBearerTokenFilter
  auth/         → login
  wallet/       → balance, transactions
  beneficiaries/→ CRUD
  transfers/    → PIX + idempotency
  common/       → ApiError, GlobalExceptionHandler, correlation filter
        │
        ▼
   PostgreSQL (Docker Compose)
```

### 4.2 Estrutura de pastas

```
springmind/
  docker-compose.yml
  pom.xml
  mvnw / mvnw.cmd
  docs/
    contracts/vuemind-wallet-openapi.yaml
    guides/entrevista-spring.md
    superpowers/specs/   # este design
    superpowers/plans/   # plano de implementação
  src/main/java/dev/springmind/wallet/
    SpringmindApplication.java
    auth/
    wallet/
    beneficiaries/
    transfers/
    security/
    common/
  src/main/resources/
    application.yml
    db/migration/V1__init.sql
  src/test/java/...
```

### 4.3 Fronteiras

| Unidade | Faz | Depende de |
|---------|-----|------------|
| `security` | Bearer mock, SecurityFilterChain | nada de feature |
| `*/api` controllers | HTTP + validação Bean Validation | services |
| `*/service` | regras de negócio (PIX, saldo) | repositories |
| `*/repository` | JPA | entidades |
| `common` | erro padrão, correlation id | — |

Regra: controllers não acessam repositórios direto.

### 4.4 Mapeamento entrevista

| Conceito citado | Onde aparece no SpringMind |
|-----------------|----------------------------|
| Spring Boot / MVC | Controllers REST |
| Spring Security | Bearer filter + SecurityConfig |
| Spring Data JPA | Repositories + entidades |
| Containers | `docker-compose.yml` (Postgres) |
| Testcontainers | Teste de integração do PIX |
| Microsserviços / idempotência | `Idempotency-Key` no PIX |
| Observabilidade básica | Actuator + correlation id |

---

## 5. Contrato e dados

- Fonte OpenAPI: cópia de `vuemind/docs/contracts/vuemind-wallet-openapi.yaml`.
- Endpoints: `POST /auth/login`, `GET /wallet/balance`, `GET /wallet/transactions`, CRUD `/beneficiaries`, `POST /transfers/pix`.
- Seed no Flyway ou `@PostConstruct`/`ApplicationRunner`: usuário demo, saldo `250_000` centavos, 1–2 favorecidos, 1 transação `PIX_IN` opcional.
- Tabelas mínimas: `users`, `accounts` (saldo), `beneficiaries`, `transactions`, `transfers`, `idempotency_keys`.
- PIX: mesma chave → mesmo transfer; sem novo débito.

---

## 6. Segurança e HTTP

- `POST /api/v1/auth/login` público; demais rotas `/api/v1/**` autenticadas.
- Token fixo `mock-jwt-demo` após login válido (igual MSW).
- `X-Correlation-Id`: ler do request ou gerar UUID; ecoar na resposta e em `ApiError`.
- CORS habilitado para dev (origens Vite/Angular locais) ou confiar no proxy dos fronts.

---

## 7. Testes

1. Teste de serviço/domínio do PIX (saldo insuficiente, idempotência) — sem rede se possível.
2. `@SpringBootTest` + Testcontainers Postgres: login → PIX → saldo debitado.
3. Smoke MockMvc do login 401/200 (opcional se o de integração já cobrir).

Não bloquear entrega por cobertura ampla.

---

## 8. Documentação de entrega

- `README.md` — Docker Compose, `./mvnw spring-boot:run`, credenciais, como apontar fronts, bullets de entrevista.
- `docs/guides/entrevista-spring.md` — pitch 60s, JPA vs in-memory, Security, Testcontainers, idempotência, diferença vs `vuemind-api`.

---

## 9. Critério de pronto

1. `docker compose up -d` + `./mvnw spring-boot:run` sobe a API em `:8080`.
2. Login demo e PIX funcionam (`curl` ou front com MSW off + proxy).
3. `./mvnw test` passa (inclui Testcontainers).
4. README + guia presentes.
5. Contrato OpenAPI respeitado (mesmos paths/DTOs dos fronts).

---

## 10. Ligação com os fronts

1. Desligar MSW no front (dev).
2. Proxy Vite/`ng serve` `/api/v1` → `http://localhost:8080`.
3. Não alterar `features/*/api` nos fronts — só quem responde muda.
