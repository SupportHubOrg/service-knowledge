package com.supporthub.knowledge.messaging.outbox.core.model;

import com.supporthub.knowledge.messaging.outbox.core.OutboxException;

/** Raised when the Kafka message key cannot be resolved from an event payload. */
public class MessageKeyResolutionException extends OutboxException {

    public MessageKeyResolutionException(String message) {
        super(message);
    }

    public MessageKeyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
