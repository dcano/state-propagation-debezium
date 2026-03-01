package io.twba.rating_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReviewEntryEventsModuleTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new ReviewEntryEventsModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldDeserializeReviewEntryCreatedEvent() throws IOException {
        String eventUid = UUID.randomUUID().toString();
        String reviewEntryId = UUID.randomUUID().toString();
        String courseId = UUID.randomUUID().toString();

        String json = """
                {
                  "occurredOn": "2025-01-15T10:00:00Z",
                  "eventUid": "%s",
                  "tenantId": null,
                  "reviewEntryId": "%s",
                  "author": "john.doe",
                  "courseId": "%s",
                  "title": "My Review",
                  "review": { "stars": "FOUR", "comment": "Great course!" },
                  "createdTime": "2025-01-15T10:00:01Z",
                  "updatedTime": "2025-01-15T10:00:02Z"
                }
                """.formatted(eventUid, reviewEntryId, courseId);

        ReviewEntryCreatedEvent event = mapper.readValue(json, ReviewEntryCreatedEvent.class);

        assertAll(
                () -> assertEquals(reviewEntryId, event.getReviewEntryId()),
                () -> assertEquals("john.doe", event.getAuthor()),
                () -> assertEquals(courseId, event.getCourseId()),
                () -> assertEquals("My Review", event.getTitle()),
                () -> assertEquals(new Review(Stars.FOUR, "Great course!"), event.getReview()),
                () -> assertEquals(Instant.parse("2025-01-15T10:00:01Z"), event.getCreatedTime()),
                () -> assertEquals(Instant.parse("2025-01-15T10:00:02Z"), event.getUpdatedTime()),
                () -> assertEquals(Instant.parse("2025-01-15T10:00:00Z"), event.getOccurredOn()),
                () -> assertEquals(eventUid, event.getEventUid())
        );
    }

    @Test
    void shouldDeserializeReviewEntryCreatedEventWithNullTenantId() throws IOException {
        String json = """
                {
                  "occurredOn": "2025-01-15T10:00:00Z",
                  "eventUid": "some-uid",
                  "tenantId": null,
                  "reviewEntryId": "entry-1",
                  "author": "jane.doe",
                  "courseId": "course-1",
                  "title": "A Title",
                  "review": { "stars": "THREE", "comment": "Decent" },
                  "createdTime": "2025-01-15T09:00:00Z",
                  "updatedTime": "2025-01-15T09:00:00Z"
                }
                """;

        ReviewEntryCreatedEvent event = mapper.readValue(json, ReviewEntryCreatedEvent.class);

        assertNull(event.getTenantId());
    }

    @Test
    void shouldDeserializeReviewEntryTitleUpdatedEvent() throws IOException {
        String eventUid = UUID.randomUUID().toString();
        String reviewEntryId = UUID.randomUUID().toString();
        String courseId = UUID.randomUUID().toString();

        String json = """
                {
                  "occurredOn": "2025-06-01T08:00:00Z",
                  "eventUid": "%s",
                  "tenantId": null,
                  "title": "Updated Title",
                  "reviewEntryId": "%s",
                  "updatedAt": "2025-06-01T08:00:00Z",
                  "courseId": "%s"
                }
                """.formatted(eventUid, reviewEntryId, courseId);

        ReviewEntryTitleUpdatedEvent event = mapper.readValue(json, ReviewEntryTitleUpdatedEvent.class);

        assertAll(
                () -> assertEquals("Updated Title", event.getTitle()),
                () -> assertEquals(reviewEntryId, event.getReviewEntryId()),
                () -> assertEquals(courseId, event.getCourseId()),
                () -> assertEquals(Instant.parse("2025-06-01T08:00:00Z"), event.getUpdatedAt()),
                () -> assertEquals(Instant.parse("2025-06-01T08:00:00Z"), event.getOccurredOn()),
                () -> assertEquals(eventUid, event.getEventUid())
        );
    }

    @Test
    void shouldDeserializeReviewUpdatedEvent() throws IOException {
        String eventUid = UUID.randomUUID().toString();
        String reviewEntryId = UUID.randomUUID().toString();
        String courseId = UUID.randomUUID().toString();

        String json = """
                {
                  "occurredOn": "2025-03-10T12:00:00Z",
                  "eventUid": "%s",
                  "tenantId": null,
                  "review": { "stars": "FIVE", "comment": "Even better on reflection!" },
                  "reviewEntryId": "%s",
                  "updatedAt": "2025-03-10T12:00:00Z",
                  "courseId": "%s"
                }
                """.formatted(eventUid, reviewEntryId, courseId);

        ReviewUpdatedEvent event = mapper.readValue(json, ReviewUpdatedEvent.class);

        assertAll(
                () -> assertEquals(new Review(Stars.FIVE, "Even better on reflection!"), event.getReview()),
                () -> assertEquals(reviewEntryId, event.getReviewEntryId()),
                () -> assertEquals(courseId, event.getCourseId()),
                () -> assertEquals(Instant.parse("2025-03-10T12:00:00Z"), event.getUpdatedAt()),
                () -> assertEquals(Instant.parse("2025-03-10T12:00:00Z"), event.getOccurredOn()),
                () -> assertEquals(eventUid, event.getEventUid())
        );
    }

    @Test
    void shouldDeserializeAllStarsVariantsInReview() throws IOException {
        for (Stars stars : Stars.values()) {
            String json = """
                    {
                      "occurredOn": "2025-01-01T00:00:00Z",
                      "eventUid": "uid",
                      "review": { "stars": "%s", "comment": "comment" },
                      "reviewEntryId": "entry-1",
                      "updatedAt": "2025-01-01T00:00:00Z",
                      "courseId": "course-1"
                    }
                    """.formatted(stars.name());

            ReviewUpdatedEvent event = mapper.readValue(json, ReviewUpdatedEvent.class);

            assertEquals(stars, event.getReview().stars());
        }
    }
}
