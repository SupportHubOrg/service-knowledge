package com.supporthub.knowledge.messaging.event.registry;

public record IntegrationEventContract(
        String key,
        String eventType,
        int version
) {
}
