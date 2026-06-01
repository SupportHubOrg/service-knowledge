package com.supporthub.knowledge.messaging.outbox.core.spi;

/** Transport-neutral result of a successful send. */
public record OutboxSendResult(
        String topic,
        int partition,
        long offset
) {
}
