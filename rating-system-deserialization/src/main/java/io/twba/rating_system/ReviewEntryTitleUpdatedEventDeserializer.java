package io.twba.rating_system;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

class ReviewEntryTitleUpdatedEventDeserializer extends ReviewEntryEventDeserializer<ReviewEntryTitleUpdatedEvent> {

    ReviewEntryTitleUpdatedEventDeserializer() {
        super(ReviewEntryTitleUpdatedEvent.class);
    }

    @Override
    public ReviewEntryTitleUpdatedEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        Title title = new Title(node.get("title").asText());
        ReviewEntryId reviewEntryId = new ReviewEntryId(node.get("reviewEntryId").asText());
        Instant updatedAt = mapper.treeToValue(node.get("updatedAt"), Instant.class);
        CourseId courseId = new CourseId(node.get("courseId").asText());

        ReviewEntryTitleUpdatedEvent event = new ReviewEntryTitleUpdatedEvent(
                title, reviewEntryId, updatedAt, courseId);

        restoreBaseFields(event, node, mapper);
        return event;
    }
}
