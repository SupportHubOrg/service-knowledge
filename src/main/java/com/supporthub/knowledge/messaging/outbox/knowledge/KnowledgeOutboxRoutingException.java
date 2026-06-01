package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.messaging.outbox.core.OutboxException;

/** Raised when a knowledge event cannot be routed to a destination (e.g. missing message key). */
public class KnowledgeOutboxRoutingException extends OutboxException {

    public KnowledgeOutboxRoutingException(String message) {
        super(message);
    }
}
