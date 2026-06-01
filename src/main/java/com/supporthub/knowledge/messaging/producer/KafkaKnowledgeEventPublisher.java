package com.supporthub.knowledge.messaging.producer;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import com.supporthub.knowledge.messaging.EventEnvelope;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSaveFailedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSavedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleRejectedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

/**
 * Kafka-backed {@link KnowledgeEventPublisher}. Wraps each payload in an {@link EventEnvelope},
 * serializes it to JSON, and publishes it to the knowledge-events topic keyed by
 * {@code sourceTicketId} (so all events for one ticket land on the same partition and stay ordered).
 *
 * <p><b>Why {@link #sendAfterCommit}:</b> publishing is deferred until the surrounding DB
 * transaction commits. This avoids the dual-write problem — if the transaction rolls back, no event
 * is emitted, so consumers never see an article/draft that was never actually persisted. When
 * invoked outside a transaction the event is sent immediately. Note this is "best effort after
 * commit", not a full transactional outbox: a crash between commit and send can drop an event.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaKnowledgeEventPublisher implements KnowledgeEventPublisher {

    private static final String SOURCE = "knowledge-management-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${application.kafka.producer.knowledge-management-events}")
    private String knowledgeEventsTopic;

    @Override
    public void publishKnowledgeArticleDraftSaved(KnowledgeArticleDraftEntity draft) {
        sendAfterCommit(draft.getSourceTicketId().toString(), EventEnvelope.of(
                "KnowledgeArticleDraftSaved",
                SOURCE,
                new KnowledgeArticleDraftSavedPayload(
                        draft.getId(),
                        draft.getSourceTicketId(),
                        draft.getOrganizationId(),
                        draft.getTitle(),
                        tagIds(draft.getTags())
                )
        ));
    }

    @Override
    public void publishKnowledgeArticleDraftSaveFailed(UUID sourceTicketId,
                                                       UUID organizationId,
                                                       String failureCode,
                                                       String failureMessage) {
        sendAfterCommit(sourceTicketId.toString(), EventEnvelope.of(
                "KnowledgeArticleDraftSaveFailed",
                SOURCE,
                new KnowledgeArticleDraftSaveFailedPayload(sourceTicketId, organizationId, failureCode, failureMessage)
        ));
    }

    @Override
    public void publishKnowledgeArticlePublished(KnowledgeArticleEntity article) {
        sendAfterCommit(article.getSourceTicketId().toString(), EventEnvelope.of(
                "KnowledgeArticlePublished",
                SOURCE,
                new KnowledgeArticlePublishedPayload(
                        article.getId(),
                        article.getDraft().getId(),
                        article.getSourceTicketId(),
                        article.getOrganizationId(),
                        article.getPublishedById(),
                        article.getTitle(),
                        article.getContent(),
                        tagIds(article.getTags())
                )
        ));
    }

    @Override
    public void publishKnowledgeArticleRejected(KnowledgeArticleDraftEntity draft, UUID rejectedById) {
        sendAfterCommit(draft.getSourceTicketId().toString(), EventEnvelope.of(
                "KnowledgeArticleRejected",
                SOURCE,
                new KnowledgeArticleRejectedPayload(
                        draft.getId(),
                        draft.getSourceTicketId(),
                        draft.getOrganizationId(),
                        rejectedById,
                        draft.getRejectionReason()
                )
        ));
    }

    /** Sends after the current transaction commits, or immediately if there is no transaction. */
    private void sendAfterCommit(String key, EventEnvelope<?> envelope) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(key, envelope);
                }
            });
            return;
        }
        send(key, envelope);
    }

    private void send(String key, EventEnvelope<?> envelope) {
        try {
            var payload = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(knowledgeEventsTopic, key, payload)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.info("Published knowledge event. eventType={}, key={}, topic={}, partition={}, offset={}",
                                    envelope.eventType(),
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to publish knowledge event. eventType={}, key={}",
                                    envelope.eventType(), key, exception);
                        }
                    });
        } catch (tools.jackson.core.JacksonException exception) {
            throw new KnowledgeEventSerializationException("Failed to serialize " + envelope.eventType(), exception);
        }
    }

    private List<UUID> tagIds(List<KnowledgeArticleTagEntity> tags) {
        return tags.stream().map(KnowledgeArticleTagEntity::getTagId).toList();
    }
}
