package com.supporthub.knowledge.messaging.outbox.core.publisher;

import com.supporthub.knowledge.messaging.outbox.core.config.OutboxProperties;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventEntity;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStore;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxEventSender;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Drains pending outbox events to the configured transport.
 *
 * <p>The claimed batch is dispatched before results are recorded. Kafka preserves ordering within
 * one partition/key; different partitions may complete independently.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventStore eventStore;
    private final OutboxEventSender eventSender;
    private final OutboxProperties properties;

    public void publishPendingEvents() {
        var recovered = eventStore.requeueStuckEvents();
        if (recovered > 0) {
            log.warn("Recovered stuck outbox events. count={}", recovered);
        }

        var events = eventStore.claimNextBatch();
        var attempts = events.stream()
                .map(event -> new OutboxSendAttempt(event, eventSender.send(event.toRecord())))
                .toList();
        waitForBatchCompletion(attempts);
        for (var attempt : attempts) {
            applyResult(attempt);
        }
    }

    private void waitForBatchCompletion(List<OutboxSendAttempt> attempts) {
        try {
            CompletableFuture.allOf(attempts.stream()
                            .map(OutboxSendAttempt::result)
                            .toArray(CompletableFuture[]::new))
                    .get(properties.batchSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException exception) {
            // Individual futures are inspected below so one failed send does not hide other results.
        } catch (TimeoutException exception) {
            log.warn("Timed out waiting for outbox batch publication. batchSize={}, timeout={}",
                    attempts.size(),
                    properties.batchSendTimeout());
        }
    }

    private void applyResult(OutboxSendAttempt attempt) {
        var event = attempt.event();
        var result = attempt.result();
        if (!result.isDone()) {
            // Do not cancel: cancelling does not stop the in-flight Kafka send (and could reorder a
            // late-arriving record). With batch-send-timeout >= delivery.timeout.ms the future
            // normally resolves on its own; anything truly hung is reclaimed by requeueStuckEvents.
            log.warn("Outbox send not complete within batch timeout; leaving IN_PROGRESS. eventType={}, eventId={}, timeout={}",
                    event.getEventType(),
                    event.getEventId(),
                    properties.batchSendTimeout());
            return;
        }
        try {
            var sendResult = result.join();
            eventStore.markPublished(event.getId());
            logSuccess(event, sendResult);
        } catch (CompletionException exception) {
            markFailure(event, publishingException(event, exception));
        }
    }

    private OutboxPublishingException publishingException(OutboxEventEntity event, CompletionException exception) {
        var cause = exception.getCause() == null ? exception : exception.getCause();
        if (cause instanceof OutboxPublishingException publishingException) {
            return publishingException;
        }
        return new OutboxPublishingException("Failed to publish " + event.getEventType(), cause);
    }

    private void markFailure(OutboxEventEntity event, OutboxPublishingException exception) {
        // No error classification: a failed send means the broker is unreachable (the producer
        // already retried transient errors internally), so we always back off and retry. There is
        // no terminal FAILED state.
        eventStore.scheduleRetry(event.getId(), exception);
        log.warn("Outbox event publication failed; scheduled retry. eventType={}, eventId={}, attempts={}, errorType={}, errorMessage={}",
                event.getEventType(),
                event.getEventId(),
                event.getAttempts() + 1,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    private void logSuccess(OutboxEventEntity event, OutboxSendResult result) {
        log.info("Published outbox event. eventType={}, eventId={}, topic={}, partition={}, offset={}",
                event.getEventType(),
                event.getEventId(),
                result.topic(),
                result.partition(),
                result.offset());
    }

    private record OutboxSendAttempt(
            OutboxEventEntity event,
            CompletableFuture<OutboxSendResult> result
    ) {
    }
}
