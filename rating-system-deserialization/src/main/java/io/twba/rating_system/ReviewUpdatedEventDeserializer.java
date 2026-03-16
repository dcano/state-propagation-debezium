package io.twba.rating_system;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

class ReviewUpdatedEventDeserializer extends ReviewEntryEventDeserializer<ReviewUpdatedEvent> {

    ReviewUpdatedEventDeserializer() {
        super(ReviewUpdatedEvent.class);
    }

    @Override
    public ReviewUpdatedEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        Review review = mapper.treeToValue(node.get("review"), Review.class);
        ReviewEntryId reviewEntryId = new ReviewEntryId(node.get("reviewEntryId").asText());
        Instant updatedAt = mapper.treeToValue(node.get("updatedAt"), Instant.class);
        CourseId courseId = new CourseId(node.get("courseId").asText());

        ReviewUpdatedEvent event = new ReviewUpdatedEvent(
                review, reviewEntryId, updatedAt, courseId);

        restoreBaseFields(event, node, mapper);
        return event;
    }
}
