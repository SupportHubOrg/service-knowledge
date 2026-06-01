package com.supporthub.knowledge.messaging.event;

public record IntegrationEventContract(
        String key,
        String eventType,
        int version
) {
}
