# Demystifying Event Sourcing

## The Confusion That Keeps Happening

Ask ten developers what "event sourcing" is and at least seven will start talking about Kafka, message brokers, or event-driven microservices. This confusion is understandable — both concepts use the word "event" — but it leads to serious architectural mistakes. Let's clear it up before going further.

**Event sourcing is a persistence strategy.** It is a way to save the state of a domain entity. Nothing more. You can use event sourcing in a system that has zero message brokers, zero asynchronous communication, and zero inter-service messaging. Conversely, you can build a fully event-driven architecture without a single line of event sourcing.

The conflation happens because event sourcing and event-driven architectures pair naturally together and are often introduced alongside each other in the same blog posts and conference talks. But architecturally they solve different problems. Event-driven architecture answers "how do services communicate?" Event sourcing answers "how does a service persist its aggregates?"

With that out of the way, let's look at what event sourcing actually is, using a real system to ground every concept.

---

## What Event Sourcing Is

In a traditional persistence model, you save the *current state* of an entity. When something changes, you overwrite the previous state. The history is gone. This is what you do every time you issue an `UPDATE` statement.

In event sourcing, you save *the sequence of changes* that produced the current state. Each change is recorded as an immutable event. To know the current state of an entity, you replay its full event history.

The aggregate never holds mutable state in a database row. The database only holds a log of events.

```
Traditional:  entity_table: { id: 42, status: "approved", rating: 4.2, ... }

Event Sourced: event_store: [ CourseReviewInitialized, StarRatingAdded, StarRatingAdded, ... ]
               (current state is derived at runtime by replaying these)
```

---

## The Rating System: A Real Implementation

The project we are examining includes a `rating-system` service that persists course reviews using event sourcing. There is no `course_reviews` table with columns for the current rating. There is only an event store.

### The Core Abstraction

The entire pattern is expressed in two small interfaces in `toolkit-core`:

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

`EventStore` is purely about persistence. `EventSourced` is purely about state reconstruction. These two concerns are deliberately separated.

### The Aggregate

`CourseReview` is the aggregate for course ratings. It implements `EventSourced<CourseReview>`:

```java
// CourseReview.java
class CourseReview extends Entity implements EventSourced<CourseReview> {
    private ReviewId reviewId;
    private RatingSummary ratingSummary;
    private CourseId courseId;
    private AverageRating averageRating;
    private TenantId tenantId;

    static CourseReview initializeCourseReview(CourseId courseId, TenantId tenantId) {
        CourseReview courseReview = new CourseReview(
            ReviewId.of(UUID.randomUUID().toString()),
            RatingSummary.initialize(),
            courseId,
            AverageRating.initialize(),
            tenantId,
            null);

        // Record the event — this is the only thing that gets persisted
        courseReview.record(new CourseReviewInitializedEvent(
            Instant.now(),
            UUID.randomUUID().toString(),
            tenantId,
            courseReview.ratingSummary,
            courseReview.averageRating,
            courseReview.courseId,
            courseReview.reviewId));

        return courseReview;
    }
```

Nothing writes to a `course_reviews` table. The aggregate constructs itself, records a `CourseReviewInitializedEvent`, and that event is the thing that gets persisted.

### Hydration: Reconstructing State from Events

This is the heart of event sourcing. When the system needs a `CourseReview`, it fetches all events for that aggregate from the event store and replays them:

```java
// CourseReview.java
@Override
public Optional<CourseReview> hydrateFrom(List<Event<DomainEventPayload>> events) {
    if (Objects.isNull(events) || events.isEmpty()) return Optional.empty();

    return Optional.of(events.stream()
        .reduce(new CourseReview((long) events.size()),
            (courseReview, domainEventPayloadEvent) -> {

                if (domainEventPayloadEvent.getPayload() instanceof CourseReviewInitializedEvent e) {
                    courseReview.courseId      = new CourseId(e.getCourseId());
                    courseReview.reviewId      = new ReviewId(e.getReviewId());
                    courseReview.ratingSummary = new RatingSummary(e.getStarsRating());
                    courseReview.averageRating = new AverageRating(
                        e.getAverageNumberOfStars(),
                        e.getAverageRate(),
                        e.getTotalNumberOfReviews());
                    courseReview.tenantId      = new TenantId(e.getTenantId());
                }
                // future events (e.g. StarRatingAddedEvent) would be handled here

                return courseReview;
            },
            (a, b) -> b));
}
```

This stream reduction is a left fold over the event log. Each event mutates the aggregate's fields — but these mutations only ever happen during reconstruction, never as a side effect of a business operation. Business operations only call `record()`.

The `ReviewEntry` aggregate handles multiple event types, illustrating how state evolves:

```java
// ReviewEntry.java — hydrateFrom
return Optional.of(events.stream()
    .reduce(new ReviewEntry((long) events.size()),
        (reviewEntry, event) -> {

            if (event.getPayload() instanceof ReviewEntryCreatedEvent e) {
                reviewEntry.courseId          = new CourseId(e.getCourseId());
                reviewEntry.reviewEntryId     = new ReviewEntryId(e.getReviewEntryId());
                reviewEntry.title             = new Title(e.getTitle());
                reviewEntry.author            = new EntryAuthor(e.getAuthor());
                reviewEntry.entryCreationTime = e.getCreatedTime();
                reviewEntry.entryUpdateTime   = e.getUpdatedTime();
                reviewEntry.review            = e.getReview();

            } else if (event.getPayload() instanceof ReviewEntryTitleUpdatedEvent e) {
                reviewEntry.title           = new Title(e.getTitle());
                reviewEntry.entryUpdateTime = e.getUpdatedAt();
            }

            return reviewEntry;
        },
        (a, b) -> b));
```

Notice two things. First, the initial state of `reviewEntry` has all fields null — it is a blank shell. Second, after replaying `ReviewEntryCreatedEvent` all fields are populated. After replaying `ReviewEntryTitleUpdatedEvent` only `title` and `entryUpdateTime` change. The final state is the aggregate of all mutations across time.

### The Repository: Where It All Connects

The repository implementation is remarkably lean:

```java
// CourseReviewRepositoryEventSourced.java
class CourseReviewRepositoryEventSourced implements CourseReviewRepository {

    private final EventStore eventStore;

    @Override
    public void save(CourseReview review) {
        eventStore.appendEvents(review.getDomainEvents());
    }

    @Override
    public CourseReview retrieveForCourse(CourseId courseId) {
        return CourseReview.from(
            eventStore.retrieveEventsFor(CourseReview.class.getSimpleName(), courseId.id()));
    }

    @Override
    public boolean existsCourseReviewForCourse(CourseId courseId) {
        return !eventStore.retrieveEventsFor(
            CourseReview.class.getSimpleName(), courseId.id()).isEmpty();
    }
}
```

`save()` does not write the aggregate's current state. It writes the *new events* the aggregate recorded since it was loaded (or created). `retrieveForCourse()` fetches all events and hydrates the aggregate. `existsCourseReviewForCourse()` checks whether any events exist for that aggregate ID — a pattern possible only because of event sourcing.

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

Two operations. That is the entire persistence model: append events in, retrieve events out ordered by `event_stream_version`. There are no updates, no deletes, no joins.

Deserialization uses the stored `event_class_name` to recover the exact type:

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

This is clean and simple — and it is also where most of the long-term complexity lives, as we will discuss shortly.

### The Event Envelope

Every event in this system is wrapped in an `Event<T>` envelope that carries metadata alongside the payload:

```java
// Event.java (selected fields)
public final class Event<T extends DomainEventPayload> implements Versionable, Traceable, Routable {

    // Headers stored as a map:
    // CORRELATION_ID, ROUTING_KEY, VERSION, AGGREGATE_TYPE,
    // AGGREGATE_ID, EVENT_STREAM_VERSION, PARTITION_KEY, SOURCE

    private final Map<String, Object> header;
    private final T payload;
```

The `event_stream_version` header is critical: it records the position of each event within the aggregate's stream, making it possible to reconstruct state in the correct order and to detect concurrent writes.

---

## The Mechanics of Recording Events

The `Entity` base class contains the mechanics of event recording:

```java
// Entity.java
protected void record(Event<? extends DomainEventPayload> event) {
    event.setAggregateType(aggregateType());
    event.setAggregateId(aggregateId());
    event.setEventStreamVersion(Objects.isNull(version) ? 0 : version + events.size());
    this.events.add(event);
}
```

When a business operation fires, the aggregate calls `record()`. The event is not yet persisted — it sits in memory in `this.events`. When the repository's `save()` method is called, it passes `getDomainEvents()` to `eventStore.appendEvents()`. The recording and the persisting are cleanly separated.

This also means you can unit-test your domain logic without touching a database at all. Create an aggregate, call business methods on it, and inspect the events it recorded. The entire state machine is verifiable in-process.

---

## Pros of Event Sourcing

**Complete audit log by construction.** You never need to add audit tables or triggers. Every state change is recorded, immutably, as a first-class artifact. You can answer "what was the state of this course review at 3pm last Tuesday?" by replaying events up to that timestamp.

**Temporal queries.** Because the full history exists, you can reconstruct any past state. `ReviewEntry.hydrateFrom(events.subList(0, n))` gives you the aggregate as it was after `n` events.

**Event-driven integration is trivially available.** The events you record for persistence are the same events you can publish to a message broker. This project uses exactly that — the same domain events flow into the event store *and* through the Transactional Outbox into RabbitMQ.

**No object-relational impedance mismatch for complex aggregates.** When an aggregate has deeply nested value objects and complex invariants, mapping it to a relational schema is painful. The event-sourced aggregate is stored as events — arbitrarily complex structures that serialize cleanly to JSON.

**Optimistic concurrency without locking.** The `event_stream_version` provides a natural concurrency token. You append only if the version you loaded is still the current version. No pessimistic locks needed.

**Unit testing is pure and fast.** The `InitializeCourseReviewCommandHandlerTest` does exactly this:

```java
@Test
public void shouldInitializeCourseReviewFromCommand() {
    when(courseReviewRepository.existsCourseReviewForCourse(command.getCourseId()))
        .thenReturn(false);
    commandHandler.handle(command);
    verify(courseReviewRepository).save(courseReviewCaptor.capture());
    CourseReview actualCourseReview = courseReviewCaptor.getValue();
    assertEquals(command.getCourseId(), actualCourseReview.getCourseId());
}
```

No database, no Spring context, just domain logic.

---

## Cons and Real Challenges

**Querying is not native.** An SQL query like `SELECT AVG(rating) FROM reviews WHERE course_id = ?` does not exist in an event-sourced system. To answer that question you need a read model — a separate projection, often called a "read side" or "query model" — that processes events and materializes queryable state. This is the Command Query Responsibility Segregation (CQRS) pattern, and event sourcing almost always requires it.

**Event versioning is the hardest problem.** When `reconstituteEvent` calls `Class.forName(eventClassName)`, it depends entirely on the class name stored in the database matching a class that exists in the running JVM. Now consider what happens when:

- `CourseReviewInitializedEvent` is renamed to `CourseReviewCreatedEvent`
- A field is added to `ReviewEntryCreatedEvent`
- A field is removed

Events stored years ago must still be deserializable by code deployed today. The event store is not like a database schema you migrate once — it is a permanent contract with your past. Strategies include:

- **Upcasting**: Transform old event formats to new ones at read time before hydration. The event store returns raw records; an upcaster chain transforms them before they reach `hydrateFrom`.
- **Event versioning by type**: Store `ReviewEntryCreatedEventV1`, `ReviewEntryCreatedEventV2` as distinct types, and handle both in the `instanceof` chain.
- **Schema registries**: In serialization-heavy environments, use Avro or Protobuf with a schema registry that tracks compatibility.

In this project, `event_class_name` stores the fully qualified Java class name. This works well, but it couples the persistence format to the package structure. Renaming a class or moving it between packages would break deserialization of historical events without a migration strategy.

**Eventual consistency in read models.** If your read model is updated asynchronously from events, there is a window where reads are stale. This is inherent to the pattern and requires the team to accept and design for eventual consistency.

**Rehydration cost grows with stream length.** For aggregates with very long event histories, replaying every event to get the current state is expensive. The standard mitigation is **snapshotting**: periodically persist a snapshot of the aggregate's current state, and on load, start from the most recent snapshot instead of event zero. This adds complexity but is well understood.

**Debugging requires a different mental model.** When something is wrong with an aggregate's state, you cannot just `SELECT *` and inspect a row. You need to understand the event log, the hydration logic, and potentially the upcasting pipeline. Teams need to invest in tooling to make event streams observable.

---

## Event Sourcing vs. Event-Driven Architecture: The Summary

| Dimension | Event Sourcing | Event-Driven Architecture |
|---|---|---|
| **Scope** | Within a single service / bounded context | Between services |
| **Purpose** | How state is **persisted** | How services **communicate** |
| **Primary artifact** | Events as the source of truth for an aggregate | Events as messages flowing between services |
| **Storage** | Append-only event store | Message broker (Kafka, RabbitMQ, etc.) |
| **Consumer** | The same service that produced the events | Other services |
| **Optional?** | Completely independent choice | Completely independent choice |

This project illustrates both sides. `CourseReview` and `ReviewEntry` use event sourcing for persistence. Those same domain events are also published to RabbitMQ via the Transactional Outbox. The two concerns use the same events but for entirely different purposes. Neither requires the other.

---

## When to Use Event Sourcing

Event sourcing is the right choice when:

- **Audit history is a first-class requirement** — finance, healthcare, compliance domains
- **Temporal queries** are needed (what did this aggregate look like at time T?)
- **Complex domain logic** benefits from a functional state machine model
- **Integration via events** is already planned — the cost of the dual infrastructure is amortized

It is the wrong choice when:

- The domain is CRUD-heavy with no meaningful history (a user profile with an email address)
- The team is small and the query-side complexity of CQRS would slow delivery unacceptably
- Event versioning has not been thought through — this is a long-term commitment

---

## Conclusion

Event sourcing is a persistence pattern with a clear contract: never overwrite state, always append events, reconstruct state by replaying history. The `rating-system` in this project demonstrates the full implementation — from the `EventSourced<T>` interface to the JDBC event store to the lean repository that connects them.

Its power comes from making time a first-class dimension of your data model. Its cost comes from everything that implies: read models, event versioning, snapshot strategies, and a team willing to reason about history rather than snapshots.

It has nothing to do with Kafka. It has nothing to do with microservices communication. It is about what you write to the database — and that is a decision you can make independently of everything else in your architecture.
