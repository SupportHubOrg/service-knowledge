package com.supporthub.knowledge.messaging.inbox.core.model;

import java.time.Instant;
import java.util.UUID;

public record InboxEventMetadata(
        UUID eventId,
        String eventType,
        int version,
        Instant timestamp,
        String source
) {
}
