package com.supporthub.knowledge.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Common wrapper around every domain event published by the service. Consumers can route and
 * version events using the metadata fields without parsing the {@code payload}.
 *
 * @param eventId      unique id of this event instance (useful for consumer-side dedup)
 * @param eventType    discriminator, e.g. {@code "KnowledgeArticlePublished"}
 * @param eventVersion schema version of the payload, currently always {@code 1}
 * @param occurredAt   when the event was created
 * @param source       originating service name
 * @param payload      the event-specific body
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String source,
        T payload
) {

    /** Creates an envelope with a fresh id, version 1 and the current timestamp. */
    public static <T> EventEnvelope<T> of(String eventType, String source, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), eventType, 1, Instant.now(), source, payload);
    }
}
