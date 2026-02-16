# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Reference implementation for **consistent state propagation between microservices** using the Transactional Outbox pattern, Change Data Capture (Debezium), and event sourcing. Three services communicate via PostgreSQL CDC → RabbitMQ.

## Build & Test Commands

When expanding wildcard imports or making bulk code style changes across Java files, process ALL files in a single pass before committing. Do not verify compilation module-by-module; instead compile the whole project at once with `mvn compile` or equivalent.

```bash
# Build everything (Java 21, Maven 3.9.6+ required)
./mvnw clean install

# CI build (no Docker images)
./mvnw clean install -Pci -DskipDockerImage=true -DpublishDockerImage=false

# Run all tests
./mvnw clean test

# Run a single test class
./mvnw test -pl <module-name> -Dtest=ClassName

# Run a single test method
./mvnw test -pl <module-name> -Dtest=ClassName#methodName

# Start infrastructure (PostgreSQL 16, pgAdmin, RabbitMQ)
docker compose up
```

Maven profiles: `local`, `ci`, `postgres`, `dev`, `unsafe`, `sslclassic`, `sslbundle`

## Architecture

### Three Services

| Service | Port | Role |
|---------|------|------|
| **Course Management** | 8443 (HTTPS) | JPA-based CRUD with transactional outbox |
| **Rating System** | 9096 | Event-sourced aggregates, consumes events via AMQP |
| **State Propagation (CDC Relay)** | 9091 | Debezium embedded engine polls outbox → publishes to RabbitMQ |

### Event Processing Pipeline

```
Entity.record(event)
  → @Aspect DomainEventAppenderConcern (intercepts repository saves)
  → DomainEventAppender.append() (ThreadLocal buffer)
  → OutboxMessage persisted atomically with domain save
  → Debezium CDC polls PostgreSQL WAL
  → MessagePublisherRabbitMq → RabbitMQ
  → Rating System AMQP listener
```

### Command Handling Chain (Decorator Pattern)

```
REST Controller → CommandBus.push(command)
  → TransactionalCommandHandlerDecorator (REPEATABLE_READ, 10s timeout)
  → EnrichBufferedEventsCommandHandlerDecorator (sets correlation ID)
  → Actual CommandHandler
  → PublishBufferedEventsCommandHandlerDecorator (flushes to outbox)
```

### Module Structure (39 modules)

**Toolkit (shared infrastructure):**
- `toolkit-core` — Event model (`Event<T>` with headers), `CommandBus`, `DomainEventAppender`, base `Entity`/`EventSourced` classes
- `toolkit-cdc-debezium-postgres` — Debezium embedded engine integration
- `toolkit-outbox-jpa-postgres` — Outbox table JPA implementation
- `toolkit-event-store-jdbc-postgres` — PostgreSQL-backed event store
- `toolkit-message-publisher-rabbitmq` — RabbitMQ publisher
- `toolkit-tx-spring` — Custom `TwbaTransactionManager` wrapping Spring TX
- `toolkit-security-adapters` — mTLS support
- `toolkit-autoconfiguration` — Spring Boot auto-configuration

**Course Management** (domain → application → rest → config → repository-jpa-postgres → runtime)

**Rating System** (domain → application → adapters → rest → repository-event-sourced → amqp-listener → autoconfiguration → runtime)

### Key Patterns

- **DDD**: Strongly typed value objects (e.g., `CourseId`, `CourseTitle`), aggregates with builder pattern, validation via `ModelValidator` + Jakarta Bean Validation
- **Event Sourcing** (Rating System): `EventSourced<T>` interface, `EventStore` persistence, aggregate hydration from event streams
- **Transactional Outbox**: Domain events saved atomically with aggregates, then captured via CDC
- **CloudEvents 3.0.0**: Event envelope specification
- **Multi-tenancy**: `MultiTenantEntity` base class support

## Testing

- **JUnit 5** + **Mockito** + **AssertJ**
- **TestContainers 2.0.1** for integration tests with PostgreSQL (logical WAL enabled)
- **Awaitility** for async assertions in CDC/messaging tests
- Tests use `@ActiveProfiles({"postgres", "unsafe"})` and `@DynamicPropertySource` for container config
- Key integration tests: `CourseManagementAppTest`, `DebeziumMessageRelayTest`

## Coding Conventions

- **No wildcard imports**: Never use `import foo.*` or `import static foo.*`. Always use fully qualified, explicit imports.

## Key Dependencies

Spring Boot 3.5.10, Debezium 3.4.1.Final, PostgreSQL 16 (logical decoding), RabbitMQ 3.13.1, Flyway 11.20.3, Hypersistence Utils, Lombok, Jackson

## Workflow

After making code style, refactoring changes or new implementations, always update CLAUDE.md to reflect any new conventions established.
