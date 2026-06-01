package com.supporthub.knowledge.messaging.outbox.core;

/** Base type for failures raised by the outbox machinery. */
public abstract class OutboxException extends RuntimeException {

    protected OutboxException(String message) {
        super(message);
    }

    protected OutboxException(String message, Throwable cause) {
        super(message, cause);
    }
}
