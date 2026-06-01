package com.supporthub.knowledge.messaging.outbox.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.outbox")
public record OutboxProperties(
        int batchSize,
        long pollDelayMs,
        Duration lockAtMostFor,
        Duration lockAtLeastFor,
        Duration batchSendTimeout,
        Duration retryBackoff,
        Duration maxRetryBackoff,
        Duration stuckTimeout
) {
}
