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

These services are started up using docker compose, the corresponding ```compose.yaml``` is located in the project root. Just exectue ```docker compose up``` to hava it running.

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

Make sure to use the **"unsafe"** spring profile. Start the service by running ```mvn spring-boot:run -Dspring-boot.run.profiles=postgres,dev,unsafe``` (or mvnw wrapper) from the module ```course-management-runtime```. The "unsafe" profile ensures the APIs can be called without authentication.

#### Rating System Service

Define the following environment variables.

| Variable                  | Description                                                            | Value                  |
|---------------------------|------------------------------------------------------------------------|------------------------|
| RABBITMQ_HOST             | RabbitMQ hostname.                                                     | localhost              |
| RABBITMQ_PORT             | RabbitMQ port.                                                         | 5672                   |
| RABBITMQ_USERNAME         | RabbitMQ username.                                                     | guest                  |
| RABBITMQ_PASSWORD         | RabbitMQ password.                                                     | guest                  |
| COURSE_MANAGEMENT_URL     | Course management base URL. Must match the provided CN when using mTLS | http://localhost:9095  |

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

### Running an Example

Now it is possible to run an example. You can access both pgadmin and RabbitMQ Management Console to see what is going on:

- pgadmin:
    - URL: http://localhost:16543/login
    - Username / password: test@test.com / test
    - Database: postgres
    - Database username / password: postgres / password
- RabbitMQ Management Console:
    - URL: http://localhost:15672
    - Username / password: guest / guest

#### Create a Course

Creating a course generates a message that will be published in the ```__MR__course-management``` exchange. The *Rating System Service* has the queue ```ratingSystemInboundQueue``` bound to the exchange.

The *Rating System Service* just prints a log entry after receiving the message.

To create a course send a HTTP POST request to ```http://localhost:9095/twba/course```:

Example: 

```
POST: http://localhost:9095/twba/course
Content-Type: application/json
Body:
{
    "id": "myCourseId1",
    "title": "Course Title 1",
    "summary": "Course Summary 1",
    "description": "Course description1",
    "teacherId": "teacherId1",
    "openingDate": "2022-04-06T00:00:00.00Z",
    "publicationDate": "2022-04-03T00:00:00.00Z",
    "preRequirement": "Course pre requirement1",
    "objective": "Course objective1",
    "expectedDurationHours": "50",
    "numberOfClasses": "10",
    "status": "XX"
}
```

## mTLS between the Rating Service and the Course Management Service

mTLS allows services to interact securely with a mutually authenticated and encrypted channel. This kind of security can be used to connect the Rating System Module with the Course Management Module, for the former to retrieve the details of a course.

### Required cryptographic material

For this example to work

- The Course Management Service (acting a server) requires a public certificate and a private key.
- The Rating System Service (acting as client) also requires a public certificate and a private key.
- For both services, the public certificates must be signed by the private key of a Root CA that both services trust.

#### Self-signed CA

A self-signed CA is used to sign the public certificates of the services. Trusted CAs (e.g. Let's Encrypt) are also valid, as long as are trusted by both services.

- Execute the following command to create the Root CA public certificate and private keys: ``openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt``
- You will be asked for the RootCA Private Key password (ROOTCA_PASWORD). Define the password you want, but ensure you take note of it since will be needed later.
- Create a truststore containing the public certificate of the RootCA. This will be needed by the services to establish the mTLS connection. Use the following command: ``keytool -import -trustcacerts -noprompt -alias ca -file rootCA.crt -keystore truststore.jks`` 

#### Course Management Service mTLS configuration

This service is acting as Server, since it is exposing an endpoint that will be used by the Rating System Service to retrieve the details of a course by id. The service must hold a public certificate and private key:

- Execute the following command to generate the private key and the certificate signing request (CSR). A password for the newly generated private key (COURSE_MANAGEMENT_PASSWORD) must be provided: ``openssl req -new -newkey rsa:4096 -keyout course-management.key -out course-management.csr``
- Create a file *course-management.ext* with the following content, feel free to choose your DNS.1 value, but make sure it matches the DNS used to access the service:
```
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = course-management-127.0.0.1.nip.io
```
- Sign the CSR with the following command, you will be asked to provide the ROOTCA_PASSWORD: ``openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in course-management.csr -out course-management.crt -days 365 -CAcreateserial -extfile course-management.ext``
- The Course Management Service private key (_course-management.key_) and public x.509 certificate (_course-management.crt_) are now in place.
- Create a bundled PKCS12 archive containing both public certificate and private key. This bundle is then imported in a keystore artifact. Use the following command: ``openssl pkcs12 -export -out course-management.p12 -name "course-management" -inkey course-management.key -in course-management.crt``
- Import the PCKS12 file in a keystore with the command: ``keytool -importkeystore -srckeystore course-management.p12 -srcstoretype PKCS12 -destkeystore keystore-course-management.jks -deststoretype JKS``
- Define the following environment variables for the Course Management runtime:

| Variable              | Description                                                                               |
|-----------------------|-------------------------------------------------------------------------------------------|
| KEYSTORE_FOLDER       | Path where the file containing the key store *keystore-course-management.jks* is located. |
| TRUSTSTORE_FOLDER     | Path where the file containing the trust store store *truststore.jks* is located.         |
| KEY_STORE_PASSWORD    | Password of the key store *keystore-course-management.jks*                                |
| PRIVATE_KEY_PASSWORD  | Password of the private key of the Course Management Service (COURSE_MANAGEMENT_PASSWORD) |
| TRUST_STORE_PASSWORD  | Password of the trust store *truststore.jks*                                              |


- Start the Course Management Service with a secured configuration (i.e. removing the "unsafe" profile) and with "sslclassic" profile selected: ```mvn spring-boot:run -Dspring-boot.run.profiles=postgres,dev,sslclassic```
- With this configuration it will not be possible to create courses as before, since the client must be authenticated with mTLS. To create courses, use the unsafe profile as outlined above.
- The Course Management Service has a whitelist of allowed services in its ``application.yaml`` file under ``twba.allowed-services``. Any certificate with CN matching any of the whitelisted "service-name" and signed with the trusted root CA can be used.

#### Rating System Service mTLS configuration

This service is acting a Client. It is making the https call to retrieve the course data from the Course Management Service, when invoking the endpoint ``/rating/{courseId}``. As client, the Rating System Service also needs a public certificate and private key bundled in its own key store. To ensure the service is authenticated, the whitelisted service name **rating-service** must be provided as CN.

- Create the private key and the CSR for the Rating System Service, providing the corresponding private key password (RATING_SYSTEM_PASSWORD). Ensure to define **rating-service** as Common Name (CN): ``openssl req -new -newkey rsa:4096 -nodes -keyout rating-system.key -out rating-system.csr``
- Sign the CSR with the same root CA private key: ``openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in rating-system.csr -out rating-system.crt -days 365 -CAcreateserial``
- Bundle both the private key and public certificate in a PKCS12 file: ``openssl pkcs12 -export -out rating-system.p12 -name "rating-system" -inkey rating-system.key -in rating-system.crt``
- Create the key store for the Rating System importing the bundled p12 file, providing a password for the newly created key store: ``keytool -importkeystore -srckeystore rating-system.p12 -srcstoretype PKCS12 -destkeystore keystore-rating-system.jks -deststoretype JKS``
- Define the following environment variables for the Rating System runtime:

| Variable              | Description                                                                               |
|-----------------------|-------------------------------------------------------------------------------------------|
| KEYSTORE_FOLDER       | Path where the file containing the key store *keystore-rating-system.jks* is located. |
| TRUSTSTORE_FOLDER     | Path where the file containing the trust store store *truststore.jks* is located.         |
| KEY_STORE_PASSWORD    | Password of the key store *keystore-course-management.jks*                                |
| PRIVATE_KEY_PASSWORD  | Password of the private key of the Course Management Service (COURSE_MANAGEMENT_PASSWORD) |
| TRUST_STORE_PASSWORD  | Password of the trust store *truststore.jks*                                              |

## SSL Bundles

Thanks to [grantlittle.me](https://grantlittle.me/2024/08/16/mtls-client-authentication-with-spring-boot/), great post!

SSL bundles simplified the configuration and management of trust materials in spring boot 3.1+ based applications. SSLBundles are autoconfigured with the provided configuration, creating instances of higher order abstractions that provide access to the "classical" objects SSLContext, KeyStore, etc.

Both Course Management Service and Rating System Service can be started with classical TLS configuration or with SSLBundles using the corresponding spring profile:

| Module                    | Profiles                                                                                                                |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------|
| course-management-runtime | sslclassic - starts the application without ssl bundles.<br>sslbundle - starts the application with ssl bundles.        |
| rating-system-runtime     | sslbundle - starts the application with ssl bundles.<br>If no profile is specified, uses the classical configuration    |

With SSL Bundles the keystore jks file for the services are automatically created by spring based on the provided *.p12 bundle with public certificate and private key.