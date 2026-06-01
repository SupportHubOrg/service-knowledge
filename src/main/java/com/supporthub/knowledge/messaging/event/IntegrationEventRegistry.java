package com.supporthub.knowledge.messaging.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class IntegrationEventRegistry {

    private final IntegrationEventProperties properties;
    private final Map<Class<?>, IntegrationEventContract> cache = new ConcurrentHashMap<>();

    public IntegrationEventContract get(Class<?> payloadType) {
        return cache.computeIfAbsent(payloadType, this::resolve);
    }

    private IntegrationEventContract resolve(Class<?> payloadType) {
        var annotation = payloadType.getAnnotation(IntegrationEvent.class);
        if (annotation == null) {
            throw new IntegrationEventConfigurationException(
                    "Integration event payload must be annotated with @IntegrationEvent: " + payloadType.getName()
            );
        }

        var key = annotation.value();
        var configuredContract = contracts().get(key);
        if (configuredContract == null) {
            throw new IntegrationEventConfigurationException("Integration event contract is not configured: " + key);
        }
        if (configuredContract.eventType() == null || configuredContract.eventType().isBlank()) {
            throw new IntegrationEventConfigurationException("Integration event type is not configured: " + key);
        }
        if (configuredContract.version() < 1) {
            throw new IntegrationEventConfigurationException("Integration event version must be positive: " + key);
        }

        return new IntegrationEventContract(key, configuredContract.eventType(), configuredContract.version());
    }

    private Map<String, IntegrationEventProperties.ContractProperties> contracts() {
        if (properties.contracts() == null) {
            return Map.of();
        }
        return properties.contracts();
    }
}
