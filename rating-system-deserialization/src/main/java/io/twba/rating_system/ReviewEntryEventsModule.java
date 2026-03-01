package io.twba.rating_system;

import com.fasterxml.jackson.databind.module.SimpleModule;


public class ReviewEntryEventsModule extends SimpleModule {

    public ReviewEntryEventsModule() {
        super("ReviewEntryEventsModule");
        addDeserializer(ReviewEntryCreatedEvent.class, new ReviewEntryCreatedEventDeserializer());
        addDeserializer(ReviewEntryTitleUpdatedEvent.class, new ReviewEntryTitleUpdatedEventDeserializer());
        addDeserializer(ReviewUpdatedEvent.class, new ReviewUpdatedEventDeserializer());
    }
}
