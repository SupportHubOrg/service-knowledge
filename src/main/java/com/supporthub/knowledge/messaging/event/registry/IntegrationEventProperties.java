package com.supporthub.knowledge.messaging.event.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "application.integration-events")
public record IntegrationEventProperties(
        Map<String, ContractProperties> contracts
) {

    public record ContractProperties(
            String eventType,
            int version
    ) {
    }
}
