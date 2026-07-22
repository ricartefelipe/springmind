# SpringMind Wallet — Relatório de Implementação

**Data:** 2026-07-22  
**Branch:** `feature/springmind-wallet`  
**Base:** `2308a27` (plano de implementação)

## Status

**CONCLUÍDO** — API completa, testes verdes (`./mvnw test`: 8/8).

## Entregas

| Item | Status |
|------|--------|
| Java 21 + Spring Boot 3.3.4 | OK |
| Docker Compose Postgres 16 | OK |
| Flyway V1 schema + V2 seed | OK |
| JPA entities + repositories | OK |
| Security Bearer `mock-jwt-demo` | OK |
| Login `demo@vuemind.dev` / `demo123` | OK |
| GET balance, transactions | OK |
| CRUD beneficiaries | OK |
| POST PIX + Idempotency-Key | OK |
| GET /transfers/{id} | OK |
| ApiError + X-Correlation-Id | OK |
| Testcontainers (PIX IT) | OK |
| README + entrevista-spring.md | OK |
| OpenAPI copiado | OK |

## Commits

1. `5f15218` chore: scaffold Spring Boot 3.3 com Postgres e Docker Compose
2. `b08fe1a` feat: schema Flyway, entidades JPA e seed da carteira
3. `f5e4541` feat: Security Bearer mock e endpoint de login
4. `346ad0a` feat: saldo, extrato e CRUD de favorecidos
5. `8e9d633` feat: PIX com idempotência e teste Testcontainers
6. `6cbfed7` docs: README e guia de entrevista SpringMind

## Testes

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Classe | Cobertura |
|--------|-----------|
| AuthFlowTest | login 200/401 |
| WalletBalanceTest | 401 sem token, saldo seed 250000 |
| PixTransferTest | 409 INSUFFICIENT_FUNDS |
| PixServiceTest | idempotência + saldo insuficiente |
| PixTransferIT | login → PIX 1000 → saldo 249000 → retry idempotente |

## Paridade vuemind-api

- Mesmos códigos de erro (`INVALID_CREDENTIALS`, `UNAUTHORIZED`, `INSUFFICIENT_FUNDS`, etc.)
- Seed idêntico: user u1, R$ 2.500,00, b1/b2, t1 PIX_IN
- Ordem PIX: idempotência → beneficiário → valor → saldo → débito
- HTTP 409 para saldo insuficiente; 400 para favorecido no PIX
- Extra: `GET /transfers/{id}` (presente no OpenAPI, ausente no vuemind-api)

## Notas técnicas

- `docker-java.properties` com `api.version=1.44` para compatibilidade Docker 29 + Testcontainers 1.20
- Container Postgres singleton em static block (evita connection refused entre classes de teste)
- `@Sql reset-wallet.sql` antes de cada teste para isolar estado
- Porta 5432 local pode estar ocupada; testes usam Testcontainers; dev pode usar Postgres existente

## Como rodar

```bash
docker compose up -d   # se porta 5432 livre
./mvnw spring-boot:run
./mvnw test
```
