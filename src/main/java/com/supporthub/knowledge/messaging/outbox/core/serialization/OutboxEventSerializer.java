package com.supporthub.knowledge.messaging.outbox.core.serialization;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Turns a typed {@link IntegrationEventEnvelope} plus its {@link OutboxEventRoute} into a storable
 * {@link OutboxEventRecord}. The published JSON follows the shared service envelope:
 * {@code {"metadata": {...}, "payload": {...}}}.
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
                    objectMapper.writeValueAsString(KafkaEventEnvelope.from(event))
            );
        } catch (JacksonException exception) {
            throw new OutboxSerializationException("Failed to serialize " + event.eventType(), exception);
        }
    }

    private record KafkaEventEnvelope(
            KafkaEventMetadata metadata,
            Object payload
    ) {

        static KafkaEventEnvelope from(IntegrationEventEnvelope event) {
            return new KafkaEventEnvelope(
                    new KafkaEventMetadata(
                            event.eventId(),
                            event.eventType(),
                            event.eventVersion(),
                            event.occurredAt(),
                            event.source()
                    ),
                    event.payload()
            );
        }
    }

    private record KafkaEventMetadata(
            UUID eventId,
            String eventType,
            int version,
            Instant timestamp,
            String source
    ) {
    }
}
