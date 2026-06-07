package com.supporthub.knowledge.messaging.outbox.core.persistence;

import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted outbox event and delivery state machine:
 * {@code PENDING -> IN_PROGRESS -> SENT}, with any publish failure returning to {@code PENDING} for
 * an exponentially backed-off retry. There is no terminal failure state: delivery is retried
 * indefinitely (the Kafka producer already handles transient broker errors internally, so a failed
 * send means the broker is unreachable and the relay simply keeps trying).
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false, name = "sequence_id", insertable = false, updatable = false)
    private Long sequenceId;

    @Column(nullable = false, name = "event_id")
    private UUID eventId;

    @Column(nullable = false, name = "event_type")
    private String eventType;

    @Column(nullable = false, name = "event_version")
    private int eventVersion;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, name = "message_key")
    private String messageKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    // Reserved for distributed-tracing propagation once tracing is configured for this service.
    private String traceparent;

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public static OutboxEventEntity pending(OutboxEventRecord eventRecord) {
        return OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .eventId(eventRecord.eventId())
                .eventType(eventRecord.eventType())
                .eventVersion(eventRecord.eventVersion())
                .source(eventRecord.source())
                .topic(eventRecord.route().topic())
                .messageKey(eventRecord.route().messageKey())
                .payload(eventRecord.payload())
                .status(OutboxEventStatus.PENDING)
                .attempts(0)
                .build();
    }

    public OutboxEventRecord toRecord() {
        return new OutboxEventRecord(
                new OutboxEventRoute(topic, messageKey),
                eventId,
                eventType,
                eventVersion,
                source,
                payload
        );
    }

    public void markInProgress(Instant lockedAt) {
        requireStatus(OutboxEventStatus.PENDING, "claim");
        status = OutboxEventStatus.IN_PROGRESS;
        this.lockedAt = lockedAt;
        updatedAt = lockedAt;
    }

    public void markPublished(Instant publishedAt) {
        requireStatus(OutboxEventStatus.IN_PROGRESS, "publish");
        status = OutboxEventStatus.SENT;
        lockedAt = null;
        this.publishedAt = publishedAt;
        lastError = null;
        updatedAt = publishedAt;
    }

    public void markRetryableFailure(String errorMessage, Instant nextAttemptAt, Instant failureRecordedAt) {
        requireStatus(OutboxEventStatus.IN_PROGRESS, "retry");
        attempts++;
        status = OutboxEventStatus.PENDING;
        lockedAt = null;
        this.nextAttemptAt = nextAttemptAt;
        lastError = errorMessage;
        updatedAt = failureRecordedAt;
    }

    private void requireStatus(OutboxEventStatus expected, String operation) {
        if (status != expected) {
            throw new IllegalOutboxEventStateException(id, status, operation);
        }
    }

}
