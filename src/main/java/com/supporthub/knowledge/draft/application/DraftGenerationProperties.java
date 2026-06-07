package com.supporthub.knowledge.draft.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Tuning for the asynchronous draft content generation worker.
 *
 * @param batchSize     how many pending drafts to process per poll
 * @param pollDelayMs   delay between polls
 * @param lockAtMostFor ShedLock upper bound for the worker
 * @param lockAtLeastFor ShedLock lower bound for the worker
 */
@ConfigurationProperties(prefix = "application.draft-generation")
public record DraftGenerationProperties(
        int batchSize,
        long pollDelayMs,
        Duration lockAtMostFor,
        Duration lockAtLeastFor
) {
}
