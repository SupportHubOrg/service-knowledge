package com.supporthub.knowledge.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.kafka.dlt")
public record KafkaDltProperties(
        String name,
        long backoffMs,
        long maxAttempts
) {
}
