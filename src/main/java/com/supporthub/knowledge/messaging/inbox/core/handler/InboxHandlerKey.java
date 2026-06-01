package com.supporthub.knowledge.messaging.inbox.core.handler;

public record InboxHandlerKey(
        String consumer,
        String eventType,
        int version
) {
}
