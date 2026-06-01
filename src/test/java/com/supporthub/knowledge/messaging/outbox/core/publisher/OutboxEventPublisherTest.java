package com.supporthub.knowledge.messaging.outbox.core.publisher;

import com.supporthub.knowledge.messaging.outbox.core.config.OutboxProperties;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventEntity;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStatus;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStore;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxEventSender;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxSendResult;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxEventPublisherTest {

    private final OutboxEventStore store = mock(OutboxEventStore.class);
    private final OutboxEventSender sender = mock(OutboxEventSender.class);
    private final OutboxProperties properties = new OutboxProperties(
            50,
            2_000,
            Duration.ofSeconds(30),
            Duration.ZERO,
            Duration.ofSeconds(10),
            Duration.ofSeconds(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(2)
    );
    private final OutboxEventPublisher publisher = new OutboxEventPublisher(store, sender, properties);

    @Test
    void shouldDispatchWholeBatchBeforeMarkingEventsPublished() {
        var first = event("KnowledgeArticleDraftSaved");
        var second = event("KnowledgeArticlePublished");
        when(store.claimNextBatch()).thenReturn(List.of(first, second));
        when(sender.send(any())).thenReturn(CompletableFuture.completedFuture(sendResult()));

        publisher.publishPendingEvents();

        InOrder order = inOrder(sender, store);
        order.verify(store).requeueStuckEvents();
        order.verify(store).claimNextBatch();
        order.verify(sender, times(2)).send(any());
        order.verify(store).markPublished(first.getId());
        order.verify(store).markPublished(second.getId());
    }

    @Test
    void shouldScheduleRetryAndStillPublishRestOfBatch() {
        var failed = event("KnowledgeArticleDraftSaved");
        var next = event("KnowledgeArticlePublished");
        var exception = new OutboxPublishingException("Kafka unavailable", new RuntimeException("boom"));
        when(store.claimNextBatch()).thenReturn(List.of(failed, next));
        when(sender.send(argThat(eventRecord -> eventRecord != null && eventRecord.eventId().equals(failed.getEventId()))))
                .thenReturn(CompletableFuture.failedFuture(exception));
        when(sender.send(argThat(eventRecord -> eventRecord != null && eventRecord.eventId().equals(next.getEventId()))))
                .thenReturn(CompletableFuture.completedFuture(sendResult()));

        publisher.publishPendingEvents();

        verify(store).scheduleRetry(eq(failed.getId()), any());
        verify(store).markPublished(next.getId());
    }

    private OutboxEventEntity event(String eventType) {
        return OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .eventVersion(1)
                .source("knowledge-management-service")
                .topic("knowledge-management-events")
                .messageKey(UUID.randomUUID().toString())
                .payload("{}")
                .status(OutboxEventStatus.IN_PROGRESS)
                .attempts(0)
                .createdAt(Instant.parse("2026-06-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-06-01T12:00:00Z"))
                .version(0L)
                .build();
    }

    private OutboxSendResult sendResult() {
        return new OutboxSendResult("knowledge-management-events", 0, 1L);
    }
}
