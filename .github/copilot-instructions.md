# Copilot Instructions for State Propagation Debezium

## Repository Overview

This is a Java-based reference implementation demonstrating **consistent state propagation between microservices** using Debezium for Change Data Capture (CDC) and the Transactional Outbox pattern. The project showcases how to reliably propagate domain events from one service to another when changes are committed.

**Key Technologies:**
- Language: Java 21 (required)
- Build Tool: Maven 3.9.6+ (wrapper included)
- Framework: Spring Boot 3.4.1
- Database: PostgreSQL 16 (logical replication enabled)
- Message Broker: RabbitMQ 3.13
- CDC: Debezium 2.5.4 Embedded Engine
- Messaging: Cloud Events 3.0.0
- Database Migrations: Flyway
- Testing: JUnit 5, Testcontainers

**Repository Size:** ~4.7 MB (114 Java files across 23 Maven modules)

## Critical Build Requirements

### Java Version Requirement
**ALWAYS use Java 21.** The project will NOT compile with Java 17 or earlier.

```bash
# Set Java 21 before any Maven command
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64

# Verify version
java -version  # Must show version 21
```

**Error if wrong Java version:** `Fatal error compiling: error: invalid target release: 21`

### Maven Wrapper
The Maven wrapper (`mvnw`) is available and must be made executable before first use:
```bash
chmod +x mvnw
./mvnw --version
```

## Build Commands

**ALWAYS run commands from the repository root directory.**

### Clean Build (Fast - ~30-40 seconds)
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
mvn clean install -DskipTests=true
```

### Full Build with Tests (~45-50 seconds)
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
mvn clean install
```

### Run Tests Only
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
mvn test
```

### CI Build (matches GitHub Actions)
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
mvn -B clean install -Pci -DskipDockerImage=true -DpublishDockerImage=false
```

**Build Order:** Maven reactor builds 23 modules in dependency order automatically. No need to build modules individually.

## Architecture and Module Structure

The project follows a modular, hexagonal architecture pattern with three microservices:

### Three Runtimes (Microservices)

1. **course-management-runtime** - Course Management Service (Port 8443/9095)
   - Domain: Managing course definitions for an e-learning platform
   - Main class: `io.twba.course_management.CourseManagementApp`
   - Database: PostgreSQL with Flyway migrations
   - Uses transactional outbox pattern

2. **rating-system-runtime** - Rating System Service
   - Domain: Course rating functionality
   - Main class: `io.twba.rating_system.RatingSystemApp`
   - Consumes events from RabbitMQ

3. **toolkit-state-propagation-debezium-runtime** - State Propagation Service
   - Main class: `io.twba.tk.cdc.message_relay.StatePropagationDebeziumApp`
   - Tails the outbox table using Debezium CDC
   - Publishes Cloud Events to RabbitMQ

### Domain isolation

- Use package private for all the domain classes (aggregates, value objects, ports, etc)
- Glue between the domain and the external (driver) adapters are the commands:
    - Commands has primitive types or publicly accessible DTOs in the constructor.
    - Internal command properties are package private using domain classes.
    - Command handlers are always package private.

### Architecture checks

- It is not allowed to call the domain from driver adapters (e.g. REST APIs) - Use always commands or queries executed via command bus / query bus.
- It is not allowed to call the repository form the domain aggregates / entities.
- Value objects must perform its own checks.
- It is not allowed direct inter service calls (Domain Isolation must prevent this from happening)
- When calling an external service, use always a port defined in the domain.

### Module Categories

**Toolkit Modules** (reusable infrastructure):
- `toolkit-core` - Core domain abstractions
- `toolkit-cdc-debezium-postgres` - Debezium CDC implementation
- `toolkit-outbox-jpa-postgres` - Outbox pattern implementation with Flyway migrations
- `toolkit-message-publisher-rabbitmq` - RabbitMQ publisher
- `toolkit-tx-spring` - Transaction management
- `toolkit-security-adapters` - mTLS security

**Course Management Modules:**
- `course-management-domain` - Domain model
- `course-management-application` - Use cases
- `course-management-repository-jpa` - Repository interfaces
- `course-management-repository-jpa-postgres` - PostgreSQL implementation
- `course-management-repository-test` - Test utilities
- `course-management-rest` - REST API
- `course-management-config` - Configuration
- `course-management-runtime` - Executable service

**Rating System Modules:**
- `rating-system-domain` - Domain model
- `rating-system-application` - Use cases
- `rating-system-adapters` - External adapters
- `rating-system-amqp-listener` - RabbitMQ listener
- `rating-system-rest` - REST API
- `rating-system-runtime` - Executable service

## Running the Services

### Prerequisites
Start infrastructure with Docker Compose:
```bash
docker compose up -d
```

This starts:
- PostgreSQL on port 5432 (with logical replication: `wal_level=logical`)
- RabbitMQ on ports 5672, 15672 (management UI)
- pgAdmin on port 16543

### Start Order (Important!)
**ALWAYS start course-management-runtime first** so Flyway creates the outbox table before the state propagation service tries to tail it.

#### Course Management Service
```bash
cd course-management-runtime
export POSTGRES_DB_NAME=postgres
export POSTGRES_DB_USER=postgres
export POSTGRES_DB_PASSWORD=password
export POSTGRES_SERVICE_NAME=localhost
export POSTGRES_SERVICE_PORT=5432
mvn spring-boot:run -Dspring-boot.run.profiles=postgres,dev,unsafe
```
Port: 9095 (with `unsafe` profile for unauthenticated access)

#### State Propagation Service
```bash
cd toolkit-state-propagation-debezium-runtime
export CDC_DB_NAME=postgres
export CDC_HOST=localhost
export CDC_DB_USER=postgres
export CDC_DB_PASSWORD=password
export CDC_OUTBOX_TABLE=outbox_schema.outbox
mvn spring-boot:run
```

#### Rating System Service
```bash
cd rating-system-runtime
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export COURSE_MANAGEMENT_URL=http://localhost:9095
mvn spring-boot:run
```

## Configuration Files

**Application configs:** Each runtime has multiple Spring profile-specific YAML files:
- `application.yaml` - Base configuration
- `application-postgres.yaml` - PostgreSQL settings (course-management)
- `application-unsafe.yaml` - Development mode without authentication
- `application-sslbundle.yaml` - mTLS with SSL Bundles (Spring Boot 3.1+)
- `application-sslclassic.yaml` - mTLS classic configuration

**Flyway migrations:**
- Course Management: `course-management-repository-jpa-postgres/src/main/resources/db/migration/`
- Outbox Table: `toolkit-outbox-jpa-postgres/src/main/resources/db/migration/`

**Build configuration:**
- Root POM: `pom.xml` (parent aggregator with 23 modules)
- Each module has its own `pom.xml`
- Profiles: `local`, `ci`

## CI/CD Pipeline

**GitHub Actions:** `.github/workflows/main.yaml`

Triggered on: Push/PR to `main` branch

Steps:
1. Checkout code
2. Setup JDK 21 (Temurin distribution)
3. Build: `mvn -B clean install -Pci -DskipDockerImage=true -DpublishDockerImage=false`
4. Docker image build for course-management-runtime (when not skipped)
5. Push to AWS ECR (eu-south-2)

**Build time:** ~40-50 seconds on CI

## Testing

**Test framework:** JUnit 5 with Testcontainers for integration tests

Tests are primarily in:
- `toolkit-cdc-debezium-postgres/src/test/`
- `toolkit-outbox-jpa-postgres/src/test/`
- `course-management-application/src/test/`
- `course-management-runtime/src/test/` (performance tests)

**No linting tools** (no Checkstyle, PMD, or SpotBugs configured).

## Key Facts and Gotchas

1. **Java 21 is mandatory** - Build fails with older Java versions
2. **Start course-management-runtime first** - Creates required outbox table
3. **mvnw needs execute permissions** - Run `chmod +x mvnw` first
4. **PostgreSQL requires logical replication** - compose.yaml sets `wal_level=logical`
5. **Maven wrapper version:** 3.9.6 (meets requirement)
6. **No separate test command needed** - `mvn install` runs tests by default
7. **Spring profiles:** Use `unsafe` for development, `sslbundle` or `sslclassic` for mTLS
8. **Outbox cleanup:** TODO exists in code - not yet implemented
9. **Docker volumes:** Stored in `.temp/` directory (gitignored)

## Repository Root Contents

```
.github/              - CI workflows and this file
.mvn/                 - Maven wrapper configuration
compose.yaml          - Docker Compose for infrastructure
mvnw, mvnw.cmd        - Maven wrapper scripts
pom.xml               - Parent POM (aggregator)
README.md             - Detailed setup and mTLS instructions
[23 module directories]
```

## Quick Reference

**Test the project:** `mvn test` (requires Java 21)  
**Clean build:** `mvn clean install -DskipTests=true`  
**CI build:** `mvn -B clean install -Pci -DskipDockerImage=true -DpublishDockerImage=false`  
**Start infra:** `docker compose up -d`  
**Create course:** `POST http://localhost:9095/twba/course` (see README for payload)

## Instructions for Agents

- **ALWAYS set JAVA_HOME to Java 21** before running any Maven command
- **ALWAYS run builds from repository root** (where parent pom.xml is)
- **Trust these instructions** - Only search for additional information if instructions are incomplete or incorrect
- **Test with the same command as CI** to ensure your changes won't break the build
- **Start services in the correct order** when testing the full system
- **Use `mvn -DskipTests` for faster iteration** when not changing test code
