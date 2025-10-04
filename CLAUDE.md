# Price Advisor - AI Agent Context

## Project Overview

**Price Advisor** is an event-driven microservices platform for analyzing product pricing from quotations using AI.

**Tech Stack**:
- **Backend**: Kotlin + Spring Boot 3.5
- **Event Streaming**: Spring Cloud Stream + RabbitMQ
- **Databases**: PostgreSQL (per service), ClickHouse (analytics)
- **AI/ML**: Spring AI + Ollama (Mistral), Milvus vector store
- **Security**: OAuth2 (Keycloak)
- **API Gateway**: Envoy Proxy
- **Observability**: OpenTelemetry + Jaeger
- **Events**: CloudEvents format
- **Patterns**: Transactional Outbox, Idempotency

---

## Architecture

### Microservices

| Service | Port | Database | Purpose                                                      |
|---------|------|----------|--------------------------------------------------------------|
| `service-quotation-agent` | 8085 | PostgreSQL:5435 | Process quotations, extract data from images/PDFs using AI   |
| `service-product-admin` | 8083 | PostgreSQL:5433 | Manage product catalog (CRUD)                                |
| `service-product-analytics` | 8084 | PostgreSQL:5434 | Analytics and reporting                                      |
| `service-bff-gateway` | 8086 | - | Backend for Frontend (Not used yet, replaced by envoy proxy) |
| `frontend` | TBD | - | React frontend                                               |

### Shared Libraries

- **`lib-events`**: Shared event definitions (CloudEvents format), event factory utilities

---

## Context for AI Assistants

**When helping with this project**:
1. Always use **transactional outbox pattern** for event publishing
2. Add **idempotency checks** to all event consumers
3. Use **CloudEvents format** for all events
4. Include **OpenTelemetry correlation IDs** in events
5. Follow **@PreAuthorize** patterns for endpoint security
6. Prefer **reactive Flux** for streaming endpoints
7. Keep events in `lib-events` for shared access
8. Add **@Transactional** to methods that publish events
9. Version event topics with `.v1`, `.v2` suffixes
10. Document event contracts (triggers, consumers, guarantees)
