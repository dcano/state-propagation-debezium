# Consistent State Propagation Between Microservices With Debezium

This project provides a reference implementation of how to *consistently* propagate state between microservices.

- **Consistent State Propagation:** Given two services, *Service A* and *Service B*, changes in the state of any of the services (e.g. *Service A*) must be notified to external services (e.g. *Service B*) when changes are actually commited by *Service A*.

The propagation of the state is based on the domain events generated by each service, leveraging Debezium Embedded Engine as Change Data Capture and the [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) pattern to ensure consistency in the publication of the events.

## How to compile the code

The projects requires Java 21 and Maven version 3.9.6 or greater. You can use your own maven installation or use the provided wrapper.

This is a multi-module maven project, execute standard maven goals for compilation and packaging. Spring Boot is used for the runtime and adapters (e.g. repository implementation with Spring Data)

To compile and generate the runtime binaries just go to the /state-propagation-debezium-parent and execute ```mvnw clean install```

## Running the example

The project has three runtimes (i.e. microservices):

- **course-management-runtime:** Course Management Service. This service encapsulates the bounded context for managing the definition of courses for a hypothetical e-learning platform.
- **rating-system-runtime:** Rating System Service. Course rating service of the e-learning platform.
- **state-propagation-debezium-runtime:** State Propagation Service. Supporting runtime which is in charge of tailing the events commited in the outbox table of the *Course Management Service* and publish those events to a RabbitMQ instance where the Rating System Service is subscribed for messages. Publishes messages follow the [Cloud Events Specification](https://cloudevents.io/).

### Starting the infrastructure

The infrastructure needed for running the example is conformed by:

- **Postgres Database:** Used by the *Course Management Service* to persists its state as well as for adding events to the transactional outbox.
- **RabbitMQ:** Used by the State Propagation Service to publish messages.

These services are spinned up using docker compose, the corresponding ```compose.yaml``` is located in the project root. Just exectue ```docker compose up``` to hava it running.

### Starting the services

Before starting the service make sure that both the Postgres Database and the RabbitMQ instance are running.

Since the outbox table of the Course Management Service must be present for the State Propagation Service to start with the change data capture, it is recommended to start first the Course Management Service which creates its database model using Flyway. Once the transactional outbox table is in place, the services can be started in any order.

The provided values ensure the service starts up smoothly.

#### Course Management Service

Define the following environment variables. 

| Variable                  | Description                       | Value     |
|---------------------------|-----------------------------------|-----------|
| POSTGRES_DB_NAME          | Name of the database.             | postgres  |
| POSTGRES_DB_USER          | Username to access the database.  | postgres  |
| POSTGRES_DB_PASSWORD      | Password to access the database.  | password  |
| POSTGRES_SERVICE_NAME     | Database hostname:                | localhost |
| POSTGRES_SERVICE_PORT     | Database port:                    | 5432      |

Start the service by running ```mvn spring-boot:run``` (or mvnw wrapper) from the module ```course-management-runtime```

#### Rating System Service

Define the following environment variables.

| Variable                  | Description             | Value      |
|---------------------------|-------------------------|-------------
| RABBITMQ_HOST             | RabbitMQ hostname.      | localhost  |
| RABBITMQ_PORT             | RabbitMQ port.          | 5672       |
| RABBITMQ_USERNAME         | RabbitMQ username.      | guest      |
| RABBITMQ_PASSWORD         | RabbitMQ password.      | guest      |

Start the service by running ```mvn spring-boot:run``` (or mvnw wrapper) from the module ```rating-system-runtime```

#### State Propagation Service

Define the following environment variables.

| Variable                  | Description                                                                           | Value                |
|---------------------------|---------------------------------------------------------------------------------------|----------------------|
| CDC_DB_NAME               | Name of the database Debezium should connect to.                                      | postgres             |
| CDC_HOST                  | Hostname of the database instance where Debezium should connect to.                   | localhost            |
| CDC_DB_USER               | Username to be used by Debezium to connect to the database.                           | postgres             |
| CDC_DB_PASSWORD           | Password to be used by Debezium to connect to the database                            | password             |
| CDC_OUTBOX_TABLE          | Name of the table that Debezium should start tailing to start the change data capture | outbox_schema.outbox | 

Start the service by running ```mvn spring-boot:run``` (or mvnw wrapper) from the module ```state-propagation-debezium-runtime```