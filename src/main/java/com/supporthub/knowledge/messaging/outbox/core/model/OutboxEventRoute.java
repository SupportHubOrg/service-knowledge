package com.supporthub.knowledge.messaging.outbox.core.model;

/**
 * Where an outbox event must be delivered: the destination topic and the partition/ordering key.
 * Produced by an {@link com.supporthub.knowledge.messaging.outbox.core.spi.OutboxRouteResolver}.
 */
public record OutboxEventRoute(
        String topic,
        String messageKey
) {
}
