package com.supporthub.knowledge.messaging.outbox.knowledge;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "application.outbox.routing.knowledge")
public record KnowledgeOutboxRoutingProperties(
        Map<String, String> eventTopics
) {

    public KnowledgeOutboxRoutingProperties {
        eventTopics = eventTopics == null ? Map.of() : Map.copyOf(eventTopics);
    }
}
