# SpringMind Wallet API

API Spring Boot 3.3 da carteira digital VueMind — backend canônico com PostgreSQL, Flyway, Spring Security (Bearer mock) e testes com Testcontainers.

## Pré-requisitos

- Java 21
- Docker (para Postgres)

## Subir o banco

```bash
docker compose up -d
```

Postgres em `localhost:5432` — database `springmind`, user/senha `springmind`.

## Rodar a API

```bash
./mvnw spring-boot:run
```

API em `http://localhost:8080`.

## Credenciais demo

| Campo | Valor |
|-------|-------|
| Email | `demo@vuemind.dev` |
| Senha | `demo123` |
| Token | `mock-jwt-demo` |

## Testes

```bash
./mvnw test
```

Testcontainers sobe Postgres efêmero para testes de integração (PIX, saldo, login).

## Endpoints (`/api/v1`)

| Método | Rota | Auth |
|--------|------|------|
| POST | `/auth/login` | Não |
| GET | `/wallet/balance` | Bearer |
| GET | `/wallet/transactions` | Bearer |
| GET/POST | `/beneficiaries` | Bearer |
| DELETE | `/beneficiaries/{id}` | Bearer |
| POST | `/transfers/pix` | Bearer + `Idempotency-Key` |
| GET | `/transfers/{id}` | Bearer |

Contrato OpenAPI: `docs/contracts/vuemind-wallet-openapi.yaml`.

## Conectar os fronts

1. Desligue o MSW no front (modo dev).
2. Configure proxy `/api/v1` → `http://localhost:8080`.
3. Não altere `features/*/api` — só quem responde muda.

## Bullets para entrevista

- **Centavos (`long`)** — evita ponto flutuante em dinheiro.
- **Correlation id** — header `X-Correlation-Id` em request/response e erros.
- **Idempotência PIX** — `Idempotency-Key` evita débito duplicado em retry.
- **Camadas por feature** — Controller → Service → Repository (JPA).
- **Testcontainers** — teste de integração do PIX contra Postgres real.
- **Diferença vs vuemind-api** — SpringMind persiste em Postgres; vuemind-api é in-memory + demo WebFlux.

Guia detalhado: `docs/guides/entrevista-spring.md`.
