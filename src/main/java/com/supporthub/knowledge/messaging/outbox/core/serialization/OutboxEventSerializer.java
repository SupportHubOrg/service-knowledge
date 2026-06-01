package com.supporthub.knowledge.messaging.outbox.core.serialization;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Turns a typed {@link IntegrationEventEnvelope} plus its {@link OutboxEventRoute} into a storable
 * {@link OutboxEventRecord}. The whole envelope is written as JSON into {@code payload}, while the
 * key metadata fields are lifted out so the outbox table can be indexed/queried without parsing JSON.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventSerializer {

    private final ObjectMapper objectMapper;

    public OutboxEventRecord serialize(IntegrationEventEnvelope event, OutboxEventRoute route) {
        try {
            return new OutboxEventRecord(
                    route,
                    event.eventId(),
                    event.eventType(),
                    event.eventVersion(),
                    event.source(),
                    objectMapper.writeValueAsString(event)
            );
        } catch (JacksonException exception) {
            throw new OutboxSerializationException("Failed to serialize " + event.eventType(), exception);
        }
    }
}
