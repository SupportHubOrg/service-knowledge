package com.supporthub.knowledge.messaging.outbox.core.publisher;

import com.supporthub.knowledge.messaging.outbox.core.OutboxException;

/** Raised when an outbox event could not be delivered to the broker. */
public class OutboxPublishingException extends OutboxException {

    public OutboxPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
