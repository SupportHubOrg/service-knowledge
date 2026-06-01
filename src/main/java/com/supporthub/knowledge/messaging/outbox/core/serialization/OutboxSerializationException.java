package com.supporthub.knowledge.messaging.outbox.core.serialization;

import com.supporthub.knowledge.messaging.outbox.core.OutboxException;

/** Raised when a domain event cannot be serialized for storage in the outbox. */
public class OutboxSerializationException extends OutboxException {

    public OutboxSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
