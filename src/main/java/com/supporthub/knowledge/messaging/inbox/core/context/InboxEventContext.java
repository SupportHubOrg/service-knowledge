package com.supporthub.knowledge.messaging.inbox.core.context;

import java.time.Instant;
import java.util.UUID;

public record InboxEventContext(
        String consumer,
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant timestamp,
        String source,
        String topic,
        int partition,
        long offset,
        String messageKey
) {
}
