# Guia de entrevista — SpringMind Wallet

Pitch de 60 segundos e pontos técnicos para falar do backend Spring Boot da trilha VueMind.

## Pitch (60s)

> "Implementei a API da carteira digital em Spring Boot 3.3 com Java 21. Ela segue o contrato OpenAPI compartilhado com os fronts Vue/React/Angular — login, saldo, extrato, favorecidos e PIX. Persisto em PostgreSQL com Flyway e JPA; a segurança usa Bearer mock compatível com o MSW dos fronts. No PIX, uso header Idempotency-Key para evitar débito duplicado em retry, e correlation id em todos os erros. Testei o fluxo crítico com Testcontainers — sobe Postgres efêmero e valida login → PIX → saldo debitado."

## JPA vs in-memory (vuemind-api)

| Tema | vuemind-api | SpringMind |
|------|-------------|------------|
| Persistência | `InMemoryStore` (singleton) | PostgreSQL + Flyway |
| Saldo | `AtomicLong` | coluna `available_cents` + lock pessimista no PIX |
| Idempotência | `ConcurrentHashMap` | tabela `idempotency_keys` |
| Seed | `@PostConstruct` | migration `V2__seed.sql` |
| Objetivo | skeleton rápido para demo | backend canônico da trilha |

## Spring Security

- API **stateless** — sem sessão/cookie.
- Única rota pública: `POST /api/v1/auth/login`.
- Filtro `MockBearerTokenFilter` aceita exatamente `Bearer mock-jwt-demo`.
- 401 no formato `ApiError` via `RestAuthEntryPoint`.

Próximo passo natural: JWT assinado (Nimbus/JJWT) com claims de usuário e expiração — sem mudar controllers.

## Testcontainers

- Classe base `AbstractPostgresIntegrationTest` com `@Container PostgreSQLContainer`.
- `@DynamicPropertySource` injeta JDBC URL nos testes.
- `PixTransferIT`: login → PIX R$ 10,00 → saldo 249000 → retry idempotente sem novo débito.

## Idempotência no PIX

Ordem das validações (igual vuemind-api):

1. Lookup idempotency key
2. Beneficiário existe
3. Valor positivo
4. Saldo suficiente (409 `INSUFFICIENT_FUNDS`)
5. Débito + transfer + transação PIX_OUT + grava chave

## Observabilidade básica

- Actuator: `/actuator/health`
- `X-Correlation-Id`: gerado se ausente, ecoado na response e em `ApiError`

## O que citar como v2

- Outbox pattern + Kafka para eventos `PixExecuted`
- JWT real com refresh token
- Deploy cloud (EC2/ECS) — fora do escopo v1
