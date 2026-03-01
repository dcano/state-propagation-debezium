package io.twba.rating_system;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

class ReviewEntryCreatedEventDeserializer extends ReviewEntryEventDeserializer<ReviewEntryCreatedEvent> {

    ReviewEntryCreatedEventDeserializer() {
        super(ReviewEntryCreatedEvent.class);
    }

    @Override
    public ReviewEntryCreatedEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ReviewEntryId reviewEntryId = new ReviewEntryId(node.get("reviewEntryId").asText());
        Instant createdTime = mapper.treeToValue(node.get("createdTime"), Instant.class);
        Instant updatedTime = mapper.treeToValue(node.get("updatedTime"), Instant.class);
        EntryAuthor author = new EntryAuthor(node.get("author").asText());
        Review review = mapper.treeToValue(node.get("review"), Review.class);
        CourseId courseId = new CourseId(node.get("courseId").asText());
        Title title = new Title(node.get("title").asText());

        ReviewEntryCreatedEvent event = new ReviewEntryCreatedEvent(
                reviewEntryId, createdTime, updatedTime, author, review, courseId, title);

        restoreBaseFields(event, node, mapper);
        return event;
    }
}
