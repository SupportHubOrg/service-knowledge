package com.supporthub.knowledge.messaging.outbox.core.persistence;

public enum OutboxEventStatus {
    PENDING,
    IN_PROGRESS,
    SENT
}
