package com.supporthub.knowledge.messaging.outbox.core.persistence;

import com.supporthub.knowledge.messaging.outbox.core.OutboxException;

import java.util.UUID;

/** Raised when an outbox event is asked to make a state transition that its current status forbids. */
public class IllegalOutboxEventStateException extends OutboxException {

    public IllegalOutboxEventStateException(UUID id, OutboxEventStatus currentStatus, String operation) {
        super("Cannot %s outbox event %s in status %s".formatted(operation, id, currentStatus));
    }
}
