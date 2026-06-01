package com.supporthub.knowledge.messaging;

import com.supporthub.knowledge.messaging.event.IntegrationEventPayload;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Common wrapper around every integration event published by the service. Consumers can route and
 * version events using the metadata fields without parsing the {@code payload}.
 *
 * @param eventId      unique id of this event instance (useful for consumer-side dedup)
 * @param eventType    discriminator, e.g. {@code "KnowledgeArticlePublished"}
 * @param eventVersion schema version of the payload, currently always {@code 1}
 * @param occurredAt   when the event was created
 * @param source       originating service name
 * @param payload      the event-specific body
 */
public record IntegrationEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String source,
        IntegrationEventPayload payload
) {

    public static IntegrationEventEnvelope of(String eventType, String source, IntegrationEventPayload payload) {
        return new IntegrationEventEnvelope(UUID.randomUUID(), eventType, 1, Instant.now(), source, payload);
    }

    public static IntegrationEventEnvelope of(String eventType, String source, IntegrationEventPayload payload, Clock clock) {
        return new IntegrationEventEnvelope(UUID.randomUUID(), eventType, 1, clock.instant(), source, payload);
    }
}
