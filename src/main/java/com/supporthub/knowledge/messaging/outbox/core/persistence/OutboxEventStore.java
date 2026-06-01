package com.supporthub.knowledge.messaging.outbox.core.persistence;

import com.supporthub.knowledge.messaging.outbox.core.config.OutboxProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Persistence boundary for claiming pending events and recording publish outcomes.
 *
 * <p>Each method uses a short transaction; Kafka sends happen outside DB transactions.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventStore {

    private static final int ERROR_MESSAGE_LIMIT = 4000;

    private final OutboxEventRepository repository;
    private final OutboxProperties properties;
    private final Clock clock;

    @Transactional
    public List<OutboxEventEntity> claimNextBatch() {
        var now = clock.instant();
        var events = repository.lockPendingForPublishing(now, properties.batchSize());
        events.forEach(event -> event.markInProgress(now));
        return events;
    }

    @Transactional
    public void markPublished(UUID eventId) {
        repository.getReferenceById(eventId).markPublished(clock.instant());
    }

    /**
     * Returns a failed event to {@code PENDING} with an exponentially backed-off, jittered
     * {@code next_attempt_at}. There is no terminal failure: a failed send means the broker is
     * unreachable (the producer client already retried transient errors internally), so the relay
     * keeps trying. {@code attempts} doubles as the backoff exponent.
     */
    @Transactional
    public void scheduleRetry(UUID eventId, Throwable failure) {
        var event = repository.getReferenceById(eventId);
        var now = clock.instant();
        event.markRetryableFailure(errorMessage(failure), now.plus(backoffFor(event.getAttempts())), now);
    }

    private Duration backoffFor(int attempts) {
        long baseMillis = properties.retryBackoff().toMillis();
        long capMillis = properties.maxRetryBackoff().toMillis();
        int exponent = Math.min(attempts, 30);
        long scaledMillis = baseMillis << exponent;
        // A negative result means the shift overflowed; clamp to the cap.
        long cappedMillis = Math.min(scaledMillis < 0 ? capMillis : scaledMillis, capMillis);
        long jitterMillis = cappedMillis <= 0 ? 0 : ThreadLocalRandom.current().nextLong(cappedMillis / 4 + 1);
        return Duration.ofMillis(cappedMillis + jitterMillis);
    }

    @Transactional
    public int requeueStuckEvents() {
        var now = clock.instant();
        return repository.recoverStuckEvents(now.minus(properties.stuckTimeout()), now);
    }

    private String errorMessage(Throwable failure) {
        var message = failure.getMessage();
        if (message == null || message.isBlank()) {
            message = failure.getClass().getSimpleName();
        }
        return message.length() <= ERROR_MESSAGE_LIMIT ? message : message.substring(0, ERROR_MESSAGE_LIMIT);
    }
}
