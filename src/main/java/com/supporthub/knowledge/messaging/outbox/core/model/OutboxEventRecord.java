package com.supporthub.knowledge.messaging.outbox.core.model;

import java.util.UUID;

/** Serialized event envelope plus routing, independent from JPA and Kafka-specific metadata. */
public record OutboxEventRecord(
        OutboxEventRoute route,
        UUID eventId,
        String eventType,
        int eventVersion,
        String source,
        String payload
) {
}
