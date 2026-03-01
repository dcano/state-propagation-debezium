# Demystifying Event Sourcing

Event sourcing carries a reputation for being complex, exotic, and risky. Much of that reputation comes from myths — misunderstandings that make developers hesitant to adopt a pattern that accountants, banks, and ledger-based systems have relied on for centuries.

This article tackles five myths directly, grounded in a real implementation: the `ReviewEntry` aggregate from a course ratings service built with event sourcing.

---

## Myth 1: "State Is the Truth"

**The Myth:** The database row holds the true state of your entity. Overwrite it when something changes.

**The Reality:** A row is not the truth — it is a *summary* of past facts. When you issue an `UPDATE`, you are committing data amnesia. The answer to "what was this value three days ago?" is gone forever.

Consider how an accountant works. They do not erase yesterday's numbers when today's transaction arrives. They record a new line in the ledger, and the balance is *derived* from the full history. Event sourcing is that ledger.

```
Traditional:  review_entries: { id: 42, title: "Great course", updated_at: "2025-01-15" }
              (how many times was the title changed? unknown. what was it before? gone.)

Event Sourced: event_store: [ ReviewEntryCreatedEvent, ReviewEntryTitleUpdatedEvent, ... ]
               (current state is derived at runtime by replaying these — history intact)
```

In the `ReviewEntry` aggregate, no column stores the current title. The `title` field is populated during hydration by replaying events in sequence:

```java
// ReviewEntry.java — hydrateFrom
return Optional.of(events.stream()
    .reduce(new ReviewEntry((long) events.size()),
        (reviewEntry, domainEventPayloadEvent) -> {

            if (domainEventPayloadEvent.getPayload() instanceof ReviewEntryCreatedEvent event) {
                reviewEntry.courseId          = new CourseId(event.getCourseId());
                reviewEntry.reviewEntryId     = new ReviewEntryId(event.getReviewEntryId());
                reviewEntry.title             = new Title(event.getTitle());
                reviewEntry.author            = new EntryAuthor(event.getAuthor());
                reviewEntry.entryCreationTime = event.getCreatedTime();
                reviewEntry.entryUpdateTime   = event.getUpdatedTime();
                reviewEntry.review            = event.getReview();

            } else if (domainEventPayloadEvent.getPayload() instanceof ReviewEntryTitleUpdatedEvent event) {
                reviewEntry.title           = new Title(event.getTitle());
                reviewEntry.entryUpdateTime = event.getUpdatedAt();

            } else if (domainEventPayloadEvent.getPayload() instanceof ReviewUpdatedEvent event) {
                reviewEntry.review          = new Review(event.getReview().stars(), event.getReview().comment());
                reviewEntry.entryUpdateTime = event.getUpdatedAt();
            }

            return reviewEntry;
        },
        (a, b) -> b));
```

The title you see today is the final result of every `ReviewEntryCreatedEvent` and `ReviewEntryTitleUpdatedEvent` ever recorded. The ledger is intact. The history is the truth.

---

## Myth 2: "Event Sourcing and Event-Driven Architecture Are the Same Thing"

**The Myth:** If I use event sourcing, I am doing EDA. If I use Kafka, I am doing event sourcing.

**The Reality:** These are orthogonal concerns that solve different problems.

| Dimension | Event Sourcing | Event-Driven Architecture |
|---|---|---|
| **Scope** | Within a single service / bounded context | Between services |
| **Purpose** | How state is **persisted** | How services **communicate** |
| **Storage** | Append-only event store | Message broker (Kafka, RabbitMQ, etc.) |
| **Consumer** | The same service that produced the events | Other services |

The "aha" moment: you can build a fully event-sourced system that is completely synchronous and never broadcasts a single message to the outside world. Conversely, you can have a fully event-driven microservices architecture where every service uses plain `UPDATE` statements internally.

The conflation happens because the two pair naturally, and they are often introduced together in the same blog posts. This project illustrates both sides independently. `ReviewEntry` uses event sourcing purely for persistence — its events never leave the service. Separately, the `CourseManagement` service publishes domain events to RabbitMQ via the Transactional Outbox. Two different problems, two different solutions, each using the word "event" for a different thing.

The `ReviewEntry` repository has no knowledge of any message broker. It speaks only to an `EventStore`:

```java
// ReviewEntryRepositoryEventSourced.java
class ReviewEntryRepositoryEventSourced implements ReviewEntryRepository {

    private final EventStore eventStore;

    @Override
    public void save(ReviewEntry reviewEntry) {
        eventStore.appendEvents(reviewEntry.getDomainEvents());
    }

    @Override
    public Optional<ReviewEntry> retrieveReviewEntryFor(EntryAuthor author, CourseId courseId) {
        return Optional.ofNullable(ReviewEntry.from(
            eventStore.retrieveEventsFor(
                ReviewEntry.class.getSimpleName(),
                courseId.id() + ":" + author.userName())));
    }
}
```

Persistence. No brokers. No async. Nothing event-driven.

---

## How It Actually Works

Before the remaining myths, a brief tour of the mechanics.

### The Core Abstractions

Two small interfaces in `toolkit-core` express the entire pattern:

```java
// EventSourced.java
public interface EventSourced<T extends Entity> {
    Optional<T> hydrateFrom(List<Event<DomainEventPayload>> events);
}

// EventStore.java
public interface EventStore {
    void appendEvents(List<Event<? extends DomainEventPayload>> event);
    List<Event<DomainEventPayload>> retrieveEventsFor(String aggregateType, String aggregateId);
}
```

`EventSourced` handles state reconstruction. `EventStore` handles persistence. Deliberately separated.

### Recording Events

When a business operation runs, the aggregate calls `record()` from the `Entity` base class. The event is not yet persisted — it sits in memory. When `save()` is called, the recorded events are flushed to the store:

```java
// Entity.java
protected void record(Event<? extends DomainEventPayload> event) {
    event.setAggregateType(aggregateType());
    event.setAggregateId(aggregateId());
    event.setEventStreamVersion(Objects.isNull(version) ? 0 : version + events.size());
    this.events.add(event);
}
```

Here is `ReviewEntry.createNew()` — a business operation that creates the aggregate and records its first event:

```java
// ReviewEntry.java
static ReviewEntry createNew(EntryAuthor author, Review review, CourseId courseId,
                             Title title, ReviewEntryCreationService creationService) {

    if (creationService.existsForAuthorAndCourse(author, courseId)) {
        throw new ReviewEntryAlreadyExistsForCourseAndUser(courseId, author);
    }

    ReviewEntry reviewEntry = new ReviewEntry(
        ReviewEntryId.of(UUID.randomUUID().toString()),
        Instant.now(), Instant.now(),
        author, review, courseId, title, null);

    reviewEntry.record(new ReviewEntryCreatedEvent(
        reviewEntry.getReviewEntryId(),
        reviewEntry.entryCreationTime,
        reviewEntry.getEntryUpdateTime(),
        reviewEntry.author,
        reviewEntry.getReview(),
        reviewEntry.getCourseId(),
        reviewEntry.getTitle()));

    return reviewEntry;
}
```

Nothing writes to a table. The aggregate constructs itself, records the event, and returns. Persistence happens later via the repository.

Similarly, `updateTitle()` only records an event if the title actually changes:

```java
// ReviewEntry.java
void updateTitle(Title title) {
    if (!this.title.equals(title)) {
        this.title = title;
        this.record(new ReviewEntryTitleUpdatedEvent(title, reviewEntryId, Instant.now(), courseId));
    }
}
```

No event, no persistence. Identical input produces no side effects.

### The Event Store: PostgreSQL as an Append-Only Log

The storage layer uses PostgreSQL with a JSONB column for the payload:

```java
// EventStoreJdbcPostgres.java
private static final String INSERT_EVENT_SQL = """
    INSERT INTO event_sourcing_schema.event_store (
        uuid, aggregate_type, aggregate_id, type, payload,
        tenant_id, event_epoch, event_stream_version, system_epoch, event_class_name
    ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
    """;

private static final String SELECT_EVENTS_SQL = """
    SELECT type, payload, event_class_name
    FROM event_sourcing_schema.event_store
    WHERE aggregate_type = ? AND aggregate_id = ?
    ORDER BY event_stream_version ASC
    """;
```

Two SQL operations: append events in, retrieve events out ordered by `event_stream_version`. No updates, no deletes, no joins.

### The Event Envelope

Every event is wrapped in an `Event<T>` envelope that carries metadata alongside the payload:

```java
// Event.java (selected fields)
public final class Event<T extends DomainEventPayload> implements Versionable, Traceable, Routable {

    // Headers: CORRELATION_ID, ROUTING_KEY, VERSION, AGGREGATE_TYPE,
    //          AGGREGATE_ID, EVENT_STREAM_VERSION, PARTITION_KEY, SOURCE

    private final Map<String, Object> header;
    private final T payload;
```

The `event_stream_version` header records each event's position within the aggregate's stream — enabling correct ordering on reconstruction and detection of concurrent writes.

---

## The Event Store as a CDC Stream

An append-only, version-ordered table is structurally identical to a Change Data Capture stream. Every row is an immutable fact with a position (`event_stream_version`). No rows are ever updated or deleted. This makes the event store a natural source for CDC tooling — any new event appended by the write model can be captured and forwarded downstream without polling.

PostgreSQL exposes this through its Write-Ahead Log (WAL). With logical decoding enabled, a tool like Debezium can tail any table in real time and emit each `INSERT` as it is committed. Because the event store is append-only by design, there are no updates or deletes to filter out — every WAL entry is a new domain event.

This project applies the principle to the **Transactional Outbox**. The `CourseManagement` service persists domain events atomically alongside its aggregate writes into an outbox table. Debezium tails that table using the `pgoutput` logical decoding plugin, and a dedicated relay service forwards each captured record to RabbitMQ as a CloudEvent:

```
CourseManagement writes atomically:
  ┌─────────────────────────────────────────────┐
  │  BEGIN TRANSACTION                           │
  │    UPDATE domain_table ...                   │
  │    INSERT INTO outbox_schema.outbox (...)    │  ← append-only
  │  COMMIT                                      │
  └─────────────────────────────────────────────┘
            │
            ▼ PostgreSQL WAL (logical decoding / pgoutput)
            │
  ┌─────────────────────────────────────────────┐
  │  DebeziumMessageRelay (embedded engine)      │
  │    .skipped.operations = "u,d,t"             │  ← only INSERTs
  │    .table.include.list = outbox_schema.outbox│
  └─────────────────────────────────────────────┘
            │
            ▼
  CloudEventRecordChangeConsumer
    → wraps record in CloudEvents 3.0 envelope
    → MessagePublisherRabbitMq → RabbitMQ exchange
```

The relay service is a thin Spring Boot application. It configures the embedded Debezium engine with the outbox table as its target and a `CloudEventRecordChangeConsumer` as the change handler:

```java
// DebeziumConfiguration.java
@Bean
public MessageRelay debeziumMessageRelay(DebeziumProperties debeziumProperties,
                                         CdcRecordChangeConsumer cdcRecordChangeConsumer) {
    return new DebeziumMessageRelay(debeziumProperties, cdcRecordChangeConsumer);
}

@Bean
public CdcRecordChangeConsumer cdcRecordChangeConsumer(MessagePublisher messagePublisher) {
    return new CloudEventRecordChangeConsumer(messagePublisher);
}
```

On each captured `INSERT`, the consumer reads the outbox record fields, constructs a CloudEvent, and publishes it to RabbitMQ:

```java
// CloudEventRecordChangeConsumer.java
@Override
public void accept(CdcRecord cdcRecord) {
    CloudEvent event = new CloudEventBuilder()
        .withId(cdcRecord.valueOf("uuid"))
        .withType(cdcRecord.valueOf("type"))
        .withSubject(cdcRecord.valueOf("aggregate_id"))
        .withExtension(CLOUD_EVENT_PARTITION_KEY, cdcRecord.valueOf("partition_key"))
        .withExtension(CLOUD_EVENT_CORRELATION_ID, cdcRecord.valueOf("correlation_id"))
        .withData("application/json", cdcRecord.<String>valueOf("payload").getBytes(UTF_8))
        .build();

    messagePublisher.publish(event);
}
```

The Debezium configuration tells the engine to skip updates, deletes, and truncations — it only processes `INSERT` operations, which is the entire contract of an append-only outbox:

```yaml
# application.yaml (state-propagation service)
debezium:
  connector-class: "io.debezium.connector.postgresql.PostgresConnector"
  custom-props:
    "[plugin.name]": "pgoutput"
  source-database-properties:
    outbox-table: "${CDC_OUTBOX_TABLE:outbox_schema.outbox}"
  # skipped.operations: "u,d,t" configured in DebeziumConfigurationProvider
```

The same approach applies directly to the event store. Because `event_sourcing_schema.event_store` is also append-only and version-ordered, Debezium could tail it just as effectively. Any service that needs to react to `ReviewEntry` state changes — for example, to update a search index or send a notification — could subscribe to that WAL stream without the `ReviewEntry` aggregate knowing anything about its consumers.

This is where event sourcing and event-driven architecture meet, without being the same thing. The event store is a persistence mechanism. The CDC layer turns it into a broadcast channel. Each concern remains independently replaceable.

---

## Myth 3: "Storing Every Change Forever Will Kill Performance"

**The Myth:** "Storing every change will exhaust disk space and make hydration too slow to be practical."

**The Reality:** For most business aggregates this concern never materialises. A `ReviewEntry` accumulates a handful of events across its lifetime: one `ReviewEntryCreatedEvent`, a few `ReviewEntryTitleUpdatedEvent`s, a few `ReviewUpdatedEvent`s. Replaying five events is instantaneous.

For aggregates with genuinely long histories — financial accounts, workflow processes with thousands of transitions — the solution is well-understood: **snapshots**.

A snapshot is a checkpoint. Every N events you persist the aggregate's current state. On the next load, you start from the most recent snapshot and replay only the events that arrived after it. Rehydration becomes O(k) where k is the number of events since the last checkpoint — regardless of total aggregate history length.

The `event_stream_version` field already present in the event store schema makes snapshot implementation straightforward: store a snapshot at version V, and on load, fetch the snapshot at V then fetch events where `event_stream_version > V`.

Introduce snapshots when profiling shows they are needed. Not before.

---

## Myth 4: "If I Change My Data Structure, Old Events Are Broken Forever"

**The Myth:** "We cannot refactor our domain model because years of events stored in the database will fail to deserialise."

**The Reality:** Events are immutable facts about what happened. You do not migrate them like a SQL table. You **upcast** them.

An upcaster is a transformation function applied at read time. When the event store retrieves a raw record, an upcaster chain transforms it from its stored form (the old schema) to the form the current code expects, before `hydrateFrom` ever sees it. Old events are never touched.

The current deserialisation in this project uses the stored class name to recover the exact type:

```java
private Event<DomainEventPayload> reconstituteEvent(
        String eventClassName, String payloadJson, String eventType) {
    try {
        Class<?> payloadClass = Class.forName(eventClassName);
        DomainEventPayload payload = (DomainEventPayload)
            objectMapper.readValue(payloadJson, payloadClass);
        return new Event<>(payload, eventType);
    } catch (JsonProcessingException | ClassNotFoundException | ClassCastException e) {
        throw new EventStoreException(
            "Failed to deserialize event of type: " + eventClassName, e);
    }
}
```

This works cleanly, but it couples the persistence format to the package structure and class name. Three concrete strategies to handle schema evolution:

- **Upcasting:** Insert a transformation step between raw record retrieval and `hydrateFrom`. An old `ReviewEntryCreatedEvent_v1` record is mapped to `ReviewEntryCreatedEvent` before the application sees it.
- **Versioned event types:** Store `ReviewEntryCreatedEventV1` and `ReviewEntryCreatedEventV2` as distinct types, and handle both in the `instanceof` chain inside `hydrateFrom`.
- **Lenient deserialisation:** Jackson's `@JsonIgnoreProperties(ignoreUnknown = true)` makes added fields backward-compatible for free. Removed fields require an explicit strategy.

The key insight: schema migration in event sourcing is a *read-time* concern, not a *write-time* concern. History is immutable. New code learns to read old formats.

---

## Myth 5: "I Have to Learn CQRS Too"

**The Myth:** "Event sourcing requires CQRS. They arrive as a package deal."

**The Reality:** They are distinct patterns that pair well but neither requires the other.

Event sourcing defines the **write model**: how you persist an aggregate's state changes. CQRS defines a separation between write operations (commands) and read operations (queries). They solve different problems.

The practical reason they appear together: an event-sourced aggregate is not directly queryable. You cannot issue `SELECT title FROM review_entries WHERE course_id = ?` against an event store. To answer read-side questions efficiently, you project events into a read model — a denormalized, queryable view. This is the Query side of CQRS.

But CQRS is not a prerequisite for getting started with event sourcing. A simple system can have an event-sourced write model and a synchronous projection that runs in the same transaction, updating a relational read table immediately. No asynchronous pipeline, no eventual consistency, no dedicated read service.

The `ReviewEntry` command handler is a clean illustration of the write side:

```java
// CreateReviewEntryCommandHandler.java
class CreateReviewEntryCommandHandler implements CommandHandler<CreateReviewEntryCommand> {

    private final ReviewEntryRepository reviewEntryRepository;
    private final ReviewEntryCreationService reviewEntryCreationService;

    @Override
    public void handle(CreateReviewEntryCommand command) {
        ReviewEntry reviewEntry = ReviewEntry.createNew(
            command.getAuthor(),
            command.getReview(),
            command.getCourseId(),
            command.getTitle(),
            reviewEntryCreationService);
        reviewEntryRepository.save(reviewEntry);
    }
}
```

The write model knows nothing about how reads will be served. CQRS is a natural evolution as query complexity grows — not a day-one requirement.

---

## Unit Testing Is a Free Benefit

Because aggregates record events in memory before any persistence occurs, domain logic is testable with zero infrastructure:

```java
@Test
void shouldRecordReviewEntryCreatedEventOnCreation() {
    when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(false);

    ReviewEntry entry = ReviewEntry.createNew(author, review, courseId, title, reviewEntryCreationService);

    assertEquals(1, entry.getDomainEvents().size());
    ReviewEntryCreatedEvent event = assertInstanceOf(
        ReviewEntryCreatedEvent.class,
        entry.getDomainEvents().get(0).getPayload());
    assertAll("Expected ReviewEntryCreatedEvent payload",
        () -> assertEquals(author.userName(), event.getAuthor()),
        () -> assertEquals(courseId.id(), event.getCourseId()),
        () -> assertEquals(review, event.getReview()),
        () -> assertEquals(title.value(), event.getTitle()));
}
```

No database. No Spring context. Pure domain logic, verified through the events it records.

The hydration path is equally testable. Construct an event list directly, call `ReviewEntry.from()`, and assert on the resulting state:

```java
private ReviewEntry existingEntry() {
    ReviewEntryCreatedEvent createdEvent = new ReviewEntryCreatedEvent(
        new ReviewEntryId(UUID.randomUUID().toString()),
        Instant.now(), Instant.now(),
        author, review, courseId, title);
    return ReviewEntry.from(List.of(new Event<>(createdEvent)));
}
```

The aggregate's entire state machine is verifiable in-process, from creation through every mutation.

---

## Pros and Cons

| Pros | Cons |
|---|---|
| **Perfect audit log by construction** — every state change is immutable and timestamped | **Learning curve** — shift in mindset from CRUD |
| **Time travel** — reconstruct any past state by replaying up to a point in time | **Eventual consistency** — asynchronous read models lag behind writes |
| **Append-only writes are fast** — sequential inserts are the most efficient write pattern for any database | **Boilerplate** — more infrastructure code up front |
| **No object-relational impedance mismatch** — complex aggregates serialize cleanly to JSON | **Querying requires a read model** — CQRS eventually becomes necessary for non-trivial queries |
| **Optimistic concurrency without locking** — `event_stream_version` is a natural concurrency token | **Event versioning is a long-term commitment** — solvable, but requires sustained discipline |

---

## When to Use Event Sourcing

**Use it when:**

- Audit history is a first-class requirement — finance, healthcare, compliance domains
- Temporal queries are needed — "what did this aggregate look like at time T?"
- Complex domain logic benefits from a functional state machine model
- Event-driven integration is already planned — the infrastructure cost is shared

**Skip it when:**

- The domain is CRUD-heavy with no meaningful history (a user profile with an email address)
- The team cannot absorb the mindset shift and tooling investment right now
- Event versioning has not been thought through — this is a permanent contract with your past

---

## Conclusion

Event sourcing is not exotic and it is not inherently complex. It is a persistence strategy with a clear contract: never overwrite state, always append events, reconstruct state by replaying history. The pattern is older than computers — it is how ledgers work.

The five myths examined here are the main sources of unnecessary fear:

1. **State is not the truth** — the history is. State is a transient summary.
2. **Event sourcing and EDA are orthogonal** — pick one, both, or neither, independently.
3. **Storage growth is manageable** — snapshots make hydration O(k), not O(n).
4. **Schema changes are handled at read time** — immutable events plus upcasting.
5. **CQRS is a natural evolution, not a day-one requirement.**

The `ReviewEntry` aggregate in this codebase demonstrates all of this. It is a handful of files: an aggregate that calls `record()`, a repository that calls `appendEvents()`, a hydration function that is a left fold over the event log. The infrastructure is lean. The domain logic is pure. The history is intact.

It has nothing to do with Kafka. It is about what you write to the database.
