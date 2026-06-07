package com.supporthub.knowledge.messaging.outbox.core.publisher;

import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventEntity;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventRepository;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStatus;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStore;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxEventSender;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxSendResult;
import com.supporthub.knowledge.support.KnowledgeIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies ordering guarantees of the outbox claim/publish loop against a real Postgres
 * (Testcontainers), with Kafka replaced by a recording stub so failures are deterministic.
 *
 * <p>Head-of-line is enforced in the claim query: an event N+1 for a {@code (topic, message_key)} is
 * not claimed while an earlier sequence is still unfinished, so an earlier event that fails can
 * never be delivered after a later event of the same key.
 */
class OutboxOrderingIntegrationTest extends KnowledgeIntegrationTestSupport {

    private static final String TOPIC = "ordering-test-topic";
    private static final Instant T0 = Instant.parse("2026-06-01T12:00:00Z");

    private final OutboxEventPublisher publisher;
    private final OutboxEventStore store;
    private final OutboxEventRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final RecordingOutboxEventSender sender;

    @Autowired
    OutboxOrderingIntegrationTest(OutboxEventPublisher publisher,
                                  OutboxEventStore store,
                                  OutboxEventRepository repository,
                                  JdbcTemplate jdbcTemplate,
                                  RecordingOutboxEventSender sender) {
        this.publisher = publisher;
        this.store = store;
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.sender = sender;
    }

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from outbox_events");
        sender.reset();
    }

    @Test
    void earlierEventNeverDeliveredAfterLaterForSameKey() {
        var key = UUID.randomUUID().toString();
        var first = insertPending(key, T0);
        var second = insertPending(key, T0);
        sender.failNext(first.getEventId(), 1);

        drainUntilEmpty();

        assertThat(sender.delivered()).containsExactly(first.getEventId(), second.getEventId());
    }

    @Test
    void laterEventNotClaimedUntilEarlierIsSent() {
        var key = UUID.randomUUID().toString();
        var first = insertPending(key, T0);
        var second = insertPending(key, T0);

        var firstBatch = store.claimNextBatch();
        assertThat(firstBatch).extracting(OutboxEventEntity::getEventId).containsExactly(first.getEventId());

        store.markPublished(first.getId());

        var secondBatch = store.claimNextBatch();
        assertThat(secondBatch).extracting(OutboxEventEntity::getEventId).containsExactly(second.getEventId());
    }

    @Test
    void differentKeysClaimedTogether() {
        var first = insertPending(UUID.randomUUID().toString(), T0);
        var second = insertPending(UUID.randomUUID().toString(), T0);

        var batch = store.claimNextBatch();

        assertThat(batch)
                .extracting(OutboxEventEntity::getEventId)
                .containsExactlyInAnyOrder(first.getEventId(), second.getEventId());
    }

    @Test
    void failureRetriesAndKeepsOrder() {
        var key = UUID.randomUUID().toString();
        var first = insertPending(key, T0);
        var second = insertPending(key, T0);
        sender.failNext(first.getEventId(), 3);

        drainUntilEmpty();

        assertThat(sender.delivered()).containsExactly(first.getEventId(), second.getEventId());
        assertThat(statusCount(OutboxEventStatus.SENT)).isEqualTo(2);
    }

    /**
     * Repeatedly drains the outbox, making any backed-off PENDING rows immediately claimable so the
     * test does not depend on wall-clock backoff. Bounded to avoid spinning forever.
     */
    private void drainUntilEmpty() {
        for (int i = 0; i < 10 && unfinishedCount() > 0; i++) {
            jdbcTemplate.update("update outbox_events set next_attempt_at = null where topic = ? and status = 'PENDING'",
                    TOPIC);
            publisher.publishPendingEvents();
        }
        assertThat(unfinishedCount()).isZero();
    }

    private long unfinishedCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from outbox_events where topic = ? and status in ('PENDING','IN_PROGRESS')",
                Long.class,
                TOPIC);
    }

    private long statusCount(OutboxEventStatus status) {
        return jdbcTemplate.queryForObject(
                "select count(*) from outbox_events where topic = ? and status = ?",
                Long.class,
                TOPIC,
                status.name());
    }

    private OutboxEventEntity insertPending(String messageKey, Instant createdAt) {
        return repository.save(OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventType("KnowledgeArticleDraftSaved")
                .eventVersion(1)
                .source("knowledge-management-service")
                .topic(TOPIC)
                .messageKey(messageKey)
                .payload("{}")
                .status(OutboxEventStatus.PENDING)
                .attempts(0)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build());
    }

    @TestConfiguration
    static class StubSenderConfig {

        @Bean
        @Primary
        RecordingOutboxEventSender recordingOutboxEventSender() {
            return new RecordingOutboxEventSender();
        }
    }

    /** Records the order of successfully delivered event ids; programmable per-event failures. */
    static class RecordingOutboxEventSender implements OutboxEventSender {

        private final List<UUID> delivered = Collections.synchronizedList(new ArrayList<>());
        private final Map<UUID, AtomicInteger> remainingFailures = new ConcurrentHashMap<>();

        void failNext(UUID eventId, int times) {
            remainingFailures.put(eventId, new AtomicInteger(times));
        }

        List<UUID> delivered() {
            return List.copyOf(delivered);
        }

        void reset() {
            delivered.clear();
            remainingFailures.clear();
        }

        @Override
        public CompletableFuture<OutboxSendResult> send(OutboxEventRecord event) {
            if (!TOPIC.equals(event.route().topic())) {
                return CompletableFuture.completedFuture(
                        new OutboxSendResult(event.route().topic(), 0, -1));
            }
            var counter = remainingFailures.get(event.eventId());
            if (counter != null && counter.getAndDecrement() > 0) {
                return CompletableFuture.failedFuture(new OutboxPublishingException(
                        "stub send failure", new RuntimeException("broker unavailable")));
            }
            delivered.add(event.eventId());
            return CompletableFuture.completedFuture(
                    new OutboxSendResult(event.route().topic(), 0, delivered.size()));
        }
    }
}
